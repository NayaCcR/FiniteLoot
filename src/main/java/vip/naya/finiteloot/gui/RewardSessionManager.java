package vip.naya.finiteloot.gui;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vip.naya.finiteloot.config.PluginSettings;
import vip.naya.finiteloot.data.Database;
import vip.naya.finiteloot.loot.ItemStacks;

public final class RewardSessionManager implements Listener {
    private static final EnumSet<InventoryAction> TOP_EXTRACTION_ACTIONS = EnumSet.of(
            InventoryAction.PICKUP_ALL,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.MOVE_TO_OTHER_INVENTORY,
            InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_SLOT,
            InventoryAction.COLLECT_TO_CURSOR,
            InventoryAction.NOTHING);
    private final JavaPlugin plugin;
    private final Database database;
    private final java.util.function.Supplier<PluginSettings> settings;
    private final Map<UUID, RewardInventoryHolder> sessions = new ConcurrentHashMap<>();

    public RewardSessionManager(
            JavaPlugin plugin, Database database, java.util.function.Supplier<PluginSettings> settings) {
        this.plugin = plugin;
        this.database = database;
        this.settings = settings;
    }

    public void open(Player player, UUID containerId, ItemStack[] contents, Component title) {
        int size = normalizedSize(contents.length);
        RewardInventoryHolder holder = new RewardInventoryHolder(containerId, player.getUniqueId());
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.inventory(inventory);
        ItemStack[] fitted = new ItemStack[size];
        System.arraycopy(contents, 0, fitted, 0, Math.min(contents.length, size));
        inventory.setContents(fitted);
        sessions.put(player.getUniqueId(), holder);
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RewardInventoryHolder holder)) {
            return;
        }
        if (!holder.playerId().equals(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        int rawSlot = event.getRawSlot();
        boolean clickedTop = rawSlot >= 0 && rawSlot < top.getSize();
        boolean insertsFromBottom = rawSlot >= top.getSize()
                && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY;
        if (settings.get().preventItemInsertion()
                && ((clickedTop && !TOP_EXTRACTION_ACTIONS.contains(event.getAction())) || insertsFromBottom)) {
            event.setCancelled(true);
            return;
        }
        scheduleSave(holder);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RewardInventoryHolder holder)) {
            return;
        }
        if (settings.get().preventItemInsertion()
                && event.getRawSlots().stream().anyMatch(slot -> slot < top.getSize())) {
            event.setCancelled(true);
            return;
        }
        scheduleSave(holder);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RewardInventoryHolder holder) {
            sessions.remove(holder.playerId(), holder);
            save(holder);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        RewardInventoryHolder holder = sessions.remove(event.getPlayer().getUniqueId());
        if (holder != null) {
            save(holder);
        }
    }

    public CompletableFuture<Void> flushAll() {
        CompletableFuture<?>[] futures = sessions.values().stream()
                .map(this::save)
                .toArray(CompletableFuture[]::new);
        sessions.clear();
        return CompletableFuture.allOf(futures);
    }

    public void closeContainer(UUID containerId) {
        sessions.values().stream()
                .filter(holder -> holder.containerId().equals(containerId))
                .map(holder -> Bukkit.getPlayer(holder.playerId()))
                .filter(java.util.Objects::nonNull)
                .forEach(Player::closeInventory);
    }

    public void closePlayerContainer(UUID playerId, UUID containerId) {
        RewardInventoryHolder holder = sessions.get(playerId);
        if (holder != null && holder.containerId().equals(containerId)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.closeInventory();
            }
        }
    }

    private void scheduleSave(RewardInventoryHolder holder) {
        Bukkit.getScheduler().runTask(plugin, () -> save(holder));
    }

    private CompletableFuture<Void> save(RewardInventoryHolder holder) {
        Inventory inventory = holder.getInventory();
        byte[] bytes = ItemStacks.serialize(inventory.getContents());
        boolean empty = ItemStacks.isEmpty(inventory.getContents());
        return database.saveInventory(holder.containerId(), holder.playerId(), bytes, empty)
                .whenComplete((ignored, throwable) -> {
                    if (throwable != null) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to save personal loot inventory", throwable);
                    }
                });
    }

    private int normalizedSize(int requested) {
        int bounded = Math.max(9, Math.min(54, requested));
        return ((bounded + 8) / 9) * 9;
    }
}
