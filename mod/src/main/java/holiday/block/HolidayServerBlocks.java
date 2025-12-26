package holiday.block;

import holiday.CommonEntrypoint;
import net.fabricmc.fabric.mixin.content.registry.BlockBehaviourAccessor;
import net.minecraft.block.*;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ColorCode;

import java.util.function.Function;

public final class HolidayServerBlocks {
    public static final Block REDSTONE_SAND = register("redstone_sand", settings -> new RedstoneSandBlock(new ColorCode(0xFFFF0000), settings
            .mapColor(MapColor.BRIGHT_RED)
            .instrument(NoteBlockInstrument.SNARE)
            .strength(0.5f)
            .sounds(BlockSoundGroup.SAND)));

    public static final Block TINY_POTATO = register("tiny_potato", settings -> new TinyPotatoBlock(settings
            .mapColor(MapColor.RAW_IRON_PINK)
            .breakInstantly()
            .nonOpaque()
            .sounds(BlockSoundGroup.CROP)));

    public static final Block ENDER_PARALYZER = register("ender_paralyzer", settings -> new EnderParalyzerBlock(settings
        .mapColor(MapColor.PALE_PURPLE)
        .strength(1.5F)
        .sounds(BlockSoundGroup.SCULK_SENSOR)
        .luminance(state -> 1)));

    public static final Block GOLDEN_HOPPER = register("golden_hopper", settings -> new GoldenHopperBlock(settings
        .requiresTool()
        .strength(3.0F, 4.8F)
        .sounds(BlockSoundGroup.METAL)
        .nonOpaque()
        .mapColor(MapColor.GOLD)));

    private HolidayServerBlocks() {
    }

    public static void register() {
        Registry.register(Registries.BLOCK_TYPE, CommonEntrypoint.identifier("redstone_sand"), RedstoneSandBlock.CODEC);
        Registry.register(Registries.BLOCK_TYPE, CommonEntrypoint.identifier("tiny_potato"), TinyPotatoBlock.CODEC);
        Registry.register(Registries.BLOCK_TYPE, CommonEntrypoint.identifier("ender_paralyzer"), EnderParalyzerBlock.CODEC);
    }

    public static Block register(String path, Function<Block.Settings, Block> factory) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, CommonEntrypoint.identifier(path));

        Block.Settings settings = Block.Settings.create().registryKey(key);
        Block block = factory.apply(settings);

        return Registry.register(Registries.BLOCK, key, block);
    }
}
