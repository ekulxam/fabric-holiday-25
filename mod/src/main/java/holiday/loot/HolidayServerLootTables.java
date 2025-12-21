package holiday.loot;

import holiday.CommonEntrypoint;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public final class HolidayServerLootTables {
    public static final RegistryKey<LootTable> HOPPER_MITE_ATTRACTION_GAMEPLAY = of("gameplay/hopper_mite_attraction");

    private HolidayServerLootTables() {
    }

    public static RegistryKey<LootTable> of(String path) {
        return RegistryKey.of(RegistryKeys.LOOT_TABLE, CommonEntrypoint.identifier(path));
    }
}
