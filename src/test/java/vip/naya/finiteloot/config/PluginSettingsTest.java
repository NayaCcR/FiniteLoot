package vip.naya.finiteloot.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PluginSettingsTest {
    @Test
    void appliesLootTableOverride() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("default-max-claims", 3);
        configuration.set("per-player-limit", 1);
        configuration.set("exhausted-action", "DENY");
        configuration.set("loot-table-overrides.minecraft:chests/ancient_city", 2);
        PluginSettings settings = PluginSettings.from(configuration);
        assertEquals(2, settings.maxClaimsFor("minecraft:chests/ancient_city"));
        assertEquals(3, settings.maxClaimsFor("minecraft:chests/shipwreck_treasure"));
        assertTrue(settings.showRemainingClaims());
        assertTrue(settings.playContainerAnimation());
        assertTrue(settings.playContainerSounds());
        assertFalse(settings.preventItemInsertion());
    }

    @Test
    void remainingClaimMessageCanBeDisabled() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("per-player-limit", 1);
        configuration.set("exhausted-action", "DENY");
        configuration.set("show-remaining-claims", false);
        assertFalse(PluginSettings.from(configuration).showRemainingClaims());
    }

    @Test
    void itemInsertionCanBePreventedExplicitly() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("per-player-limit", 1);
        configuration.set("exhausted-action", "DENY");
        configuration.set("prevent-item-insertion", true);
        assertTrue(PluginSettings.from(configuration).preventItemInsertion());
    }
}
