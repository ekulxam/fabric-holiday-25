package holiday.block;

import holiday.block.blockentity.GoldenHopperBlockEntity;
import holiday.block.blockentity.HolidayServerBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class GoldenHopperBlock extends HopperBlock {
    public GoldenHopperBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GoldenHopperBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient() ? null : validateTicker(type, HolidayServerBlockEntities.GOLDEN_HOPPER_BLOCK_ENTITY, GoldenHopperBlockEntity::serverTick);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GoldenHopperBlockEntity goldenHopperBlockEntity) {
            GoldenHopperBlockEntity.onEntityCollided(world, pos, state, entity, goldenHopperBlockEntity);
        }
    }
}
