package holiday.tag;

import holiday.CommonEntrypoint;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public final class HolidayServerItemTags {
    public static final TagKey<Item> HOPPER_TRAPPED = of("hopper_trapped");

    private HolidayServerItemTags() {
    }

    public static TagKey<Item> of(String path) {
        return TagKey.of(RegistryKeys.ITEM, CommonEntrypoint.identifier(path));
    }
}
