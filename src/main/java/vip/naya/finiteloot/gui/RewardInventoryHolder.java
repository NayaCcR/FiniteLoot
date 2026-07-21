package vip.naya.finiteloot.gui;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class RewardInventoryHolder implements InventoryHolder {
    private final UUID containerId;
    private final UUID playerId;
    private Inventory inventory;

    public RewardInventoryHolder(UUID containerId, UUID playerId) {
        this.containerId = containerId;
        this.playerId = playerId;
    }

    public UUID containerId() {
        return containerId;
    }

    public UUID playerId() {
        return playerId;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
