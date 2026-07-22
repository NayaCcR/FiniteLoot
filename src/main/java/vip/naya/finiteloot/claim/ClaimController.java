package vip.naya.finiteloot.claim;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vip.naya.finiteloot.config.Messages;
import vip.naya.finiteloot.config.PluginSettings;
import vip.naya.finiteloot.container.ContainerTarget;
import vip.naya.finiteloot.data.ClaimAllocation;
import vip.naya.finiteloot.data.ContainerRecord;
import vip.naya.finiteloot.data.Database;
import vip.naya.finiteloot.gui.RewardSessionManager;
import vip.naya.finiteloot.loot.ItemStacks;
import vip.naya.finiteloot.loot.LootGenerator;

public final class ClaimController {
    private final JavaPlugin plugin;
    private final Database database;
    private final RewardSessionManager sessions;
    private final LootGenerator generator;
    private final java.util.function.Supplier<PluginSettings> settings;
    private final java.util.function.Supplier<Messages> messages;
    private final Set<String> inFlight = ConcurrentHashMap.newKeySet();

    public ClaimController(
            JavaPlugin plugin,
            Database database,
            RewardSessionManager sessions,
            LootGenerator generator,
            java.util.function.Supplier<PluginSettings> settings,
            java.util.function.Supplier<Messages> messages) {
        this.plugin = plugin;
        this.database = database;
        this.sessions = sessions;
        this.generator = generator;
        this.settings = settings;
        this.messages = messages;
    }

    public void open(Player player, ContainerTarget target) {
        String operationKey = target.id() + ":" + player.getUniqueId();
        if (!inFlight.add(operationKey)) {
            messages.get().send(player, "loading");
            return;
        }
        messages.get().send(player, "loading");
        database.findContainer(target.id())
                .thenCompose(existing -> existing == null
                        ? database.upsertContainer(createRecord(target))
                        : java.util.concurrent.CompletableFuture.completedFuture(existing))
                .thenCompose(record -> database.allocateClaim(
                                record.id(), player.getUniqueId(), player.getName(), shouldCount(player),
                                !settings.get().preventItemInsertion())
                        .thenCompose(allocation -> database.findContainer(record.id())
                                .thenApply(latest -> new AllocationWithRecord(
                                        latest == null ? record : latest, allocation))))
                .whenComplete((result, throwable) -> runMain(() -> {
                    if (throwable != null) {
                        fail(player, operationKey, throwable);
                        return;
                    }
                    handleAllocation(player, target, result.record(), result.allocation(), operationKey);
                }));
    }

    private void handleAllocation(
            Player player,
            ContainerTarget target,
            ContainerRecord record,
            ClaimAllocation allocation,
            String operationKey) {
        switch (allocation.kind()) {
            case EXISTING -> {
                inFlight.remove(operationKey);
                if (allocation.contents() == null) {
                    messages.get().send(player, "database-error");
                    return;
                }
                openInventory(player, target, record, ItemStacks.deserialize(allocation.contents()));
            }
            case COMPLETED -> {
                inFlight.remove(operationKey);
                messages.get().send(player, "completed");
            }
            case EXHAUSTED -> {
                inFlight.remove(operationKey);
                messages.get().send(player, "exhausted");
            }
            case PENDING -> {
                inFlight.remove(operationKey);
                messages.get().send(player, "loading");
            }
            case NOT_FOUND -> {
                inFlight.remove(operationKey);
                messages.get().send(player, "database-error");
            }
            case NEW -> persistNewInventory(player, target, record, operationKey);
            default -> throw new IllegalStateException("Unhandled allocation " + allocation.kind());
        }
    }

    private void persistNewInventory(
            Player player, ContainerTarget target, ContainerRecord record, String operationKey) {
        final ItemStack[] generated;
        try {
            generated = generator.generate(record, target, player);
        } catch (RuntimeException exception) {
            database.releasePendingClaim(record.id(), player.getUniqueId());
            fail(player, operationKey, exception);
            return;
        }
        byte[] bytes = ItemStacks.serialize(generated);
        boolean empty = ItemStacks.isEmpty(generated);
        boolean completed = empty && settings.get().preventItemInsertion();
        database.finalizeClaim(record.id(), player.getUniqueId(), bytes, completed).whenComplete((ignored, throwable) -> {
            if (throwable != null) {
                database.releasePendingClaim(record.id(), player.getUniqueId());
            }
            runMain(() -> {
                inFlight.remove(operationKey);
                if (throwable != null || !player.isOnline()) {
                    if (throwable != null) {
                        fail(player, operationKey, throwable);
                    }
                    return;
                }
                if (completed) {
                    messages.get().send(player, "completed");
                } else {
                    openInventory(player, target, record, generated);
                }
            });
        });
    }

    private ContainerRecord createRecord(ContainerTarget target) {
        String lootTable = target.lootTable() == null ? null : target.lootTable().getKey().toString();
        var primary = target.primary();
        Integer partnerX = target.tiles().size() > 1 ? target.tiles().get(1).getX() : null;
        Integer partnerY = target.tiles().size() > 1 ? target.tiles().get(1).getY() : null;
        Integer partnerZ = target.tiles().size() > 1 ? target.tiles().get(1).getZ() : null;
        return new ContainerRecord(
                target.id(), primary.getWorld().getUID(), primary.getX(), primary.getY(), primary.getZ(),
                partnerX, partnerY, partnerZ, lootTable, null,
                settings.get().maxClaimsFor(lootTable), 0, false);
    }

    private boolean shouldCount(Player player) {
        PluginSettings current = settings.get();
        if (!current.countCreativePlayers() && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }
        return !(current.adminBypassCounts() && player.hasPermission("finiteloot.bypass"));
    }

    private void openInventory(
            Player player, ContainerTarget target, ContainerRecord record, ItemStack[] contents) {
        sessions.open(player, record.id(), contents, title(), target);
        PluginSettings current = settings.get();
        if (current.showRemainingClaims()) {
            int remaining = Math.max(0, record.maxClaims() - record.claimCount());
            messages.get().send(player, "remaining-claims", java.util.Map.of(
                    "remaining", Integer.toString(remaining),
                    "max", Integer.toString(record.maxClaims())));
        }
    }

    private net.kyori.adventure.text.Component title() {
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize("<gold>FiniteLoot</gold> <gray>- Personal Loot</gray>");
    }

    private void fail(Player player, String operationKey, Throwable throwable) {
        inFlight.remove(operationKey);
        plugin.getLogger().log(Level.SEVERE, "Claim operation failed for " + player.getUniqueId(), throwable);
        if (player.isOnline()) {
            messages.get().send(player, "database-error");
        }
    }

    private void runMain(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    private record AllocationWithRecord(ContainerRecord record, ClaimAllocation allocation) {
    }
}
