package holiday.block.blockentity;

import holiday.CommonEntrypoint;
import holiday.block.HolidayServerBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class HolidayServerBlockEntities {
    public static final BlockEntityType<GoldenHopperBlockEntity> GOLDEN_HOPPER_BLOCK_ENTITY =
        register("golden_hopper", GoldenHopperBlockEntity::new, HolidayServerBlocks.GOLDEN_HOPPER);

    private static <T extends BlockEntity> BlockEntityType<T> register(
        String name,
        FabricBlockEntityTypeBuilder.Factory<? extends @NotNull T> entityFactory,
        Block... blocks
    ) {
        Identifier id = CommonEntrypoint.identifier(name);
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void register() {}
}
