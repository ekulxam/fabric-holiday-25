package holiday.item;

import holiday.CommonEntrypoint;
import holiday.block.HolidayServerBlocks;
import holiday.component.HolidayServerDataComponentTypes;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BannerPatternTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;

import java.util.function.Function;

public final class HolidayServerItems {
    public static final Item REDSTONE_SAND = register("redstone_sand", settings -> new BlockItem(HolidayServerBlocks.REDSTONE_SAND, settings
            .useBlockPrefixedTranslationKey()));

    public static final Item TINY_POTATO = register("tiny_potato", settings -> new BlockItem(HolidayServerBlocks.TINY_POTATO, settings
            .useBlockPrefixedTranslationKey()
            .equippableUnswappable(EquipmentSlot.HEAD)));

    public static final Item ENDER_PARALYZER = register("ender_paralyzer", settings -> new BlockItem(HolidayServerBlocks.ENDER_PARALYZER, settings
        .useBlockPrefixedTranslationKey()));

    public static final Item FABRIC_PATTERN_ITEM = register("fabric_banner_pattern", new Item.Settings().maxCount(1).component(DataComponentTypes.PROVIDES_BANNER_PATTERNS, patternTagOf("pattern_item/fabric")));
    public static final Item TATER_PATTERN_ITEM = register("tater_banner_pattern", new Item.Settings().maxCount(1).component(DataComponentTypes.PROVIDES_BANNER_PATTERNS, patternTagOf("pattern_item/tater")));

    public static final Item HOPPER_MITE = register("hopper_mite", settings -> new HopperMiteItem(settings
        .maxCount(1)));

    public static final Item ABSOLUTELY_SAFE_ARMOR = register("absolutely_safe_armor", new Item.Settings()
        .maxCount(1)
        .component(HolidayServerDataComponentTypes.ABSOLUTELY_SAFE, Unit.INSTANCE)
        .component(DataComponentTypes.EQUIPPABLE, EquippableComponent.builder(EquipmentSlot.CHEST)
            .damageOnHurt(false)
            .model(HolidayServerEquipmentAssetKeys.ABSOLUTELY_SAFE_ARMOR)
            .cameraOverlay(Identifier.ofVanilla("block/light_gray_stained_glass"))
            .build()
        )
    );

    public static final Item GOLDEN_HOPPER = register("golden_hopper", settings -> new BlockItem(HolidayServerBlocks.GOLDEN_HOPPER, settings
        .useBlockPrefixedTranslationKey()));

    public static Item register(String id, Item.Settings settings) {
        return register(keyOf(id), Item::new, settings);
    }

    public static Item register(RegistryKey<Item> key, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = factory.apply(settings.registryKey(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.appendBlocks(Item.BLOCK_ITEMS, item);
        }

        return Registry.register(Registries.ITEM, key, item);
    }

    private static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, CommonEntrypoint.identifier(id));
    }

    private HolidayServerItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.addAfter(Items.TURTLE_HELMET, ABSOLUTELY_SAFE_ARMOR);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) ->
                itemGroup.addAfter(Items.MOJANG_BANNER_PATTERN, FABRIC_PATTERN_ITEM, TATER_PATTERN_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addBefore(Items.SKELETON_SKULL, TINY_POTATO);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            entries.addAfter(Items.HOPPER, GOLDEN_HOPPER, HOPPER_MITE);
            entries.addAfter(Items.REDSTONE_BLOCK, REDSTONE_SAND, ENDER_PARALYZER);

	});
    }

    public static Item register(String path, Function<Item.Settings, Item> factory) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, CommonEntrypoint.identifier(path));

        Item.Settings settings = new Item.Settings().registryKey(key);
        Item item = factory.apply(settings);

        return Registry.register(Registries.ITEM, key, item);
    }


    private static TagKey<BannerPattern> patternTagOf(String id) {
        return TagKey.of(RegistryKeys.BANNER_PATTERN, CommonEntrypoint.identifier(id));
    }

    public static boolean isAbsolutelySafe(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && !entity.isSpectator() && entity.isAlive()) {
            for (EquipmentSlot slot : EquipmentSlot.VALUES) {
                ItemStack stack = livingEntity.getEquippedStack(slot);

                if (stack.contains(HolidayServerDataComponentTypes.ABSOLUTELY_SAFE)) {
                    EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);

                    if (equippable != null && slot == equippable.slot()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
