package holiday;

import com.mojang.serialization.Codec;
import holiday.block.HolidayServerBlocks;
import holiday.component.HolidayServerDataComponentTypes;
import holiday.event.EndermanParalyzeEvent;
import holiday.item.HolidayServerItems;
import holiday.loot.HolidayServerLootContextTypes;
import holiday.pond.SpeedyHopperAccess;
import holiday.sound.HolidayServerSoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LavaCauldronBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class CommonEntrypoint implements ModInitializer {
    private static final String MOD_ID = "holiday-server-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String CURRENT_VERSION = FabricLoader.getInstance()
            .getModContainer(MOD_ID)
            .get()
            .getMetadata()
            .getVersion()
            .getFriendlyString();

    private static final AttachmentType<Boolean> ANIMALS_REGENERATED_CHUNK_TYPE = AttachmentRegistry.create(
            identifier("animals_regenerated"),
            builder -> builder.initializer(() -> Boolean.FALSE).persistent(Codec.BOOL)
    );

    public static final FeatureSet FORCE_ENABLED_FEATURES = FeatureSet.of(FeatureFlags.MINECART_IMPROVEMENTS);

    @Override
    public void onInitialize() {
        HolidayServerBlocks.register();
        HolidayServerDataComponentTypes.register();
        HolidayServerItems.register();
        HolidayServerLootContextTypes.register();
        HolidayServerSoundEvents.register();

        DispenserBehavior oldBucketBehavior = DispenserBlock.BEHAVIORS.get(Items.BUCKET);
        DispenserBehavior bucketBehavior = (pointer, stack) -> {
            BlockPos pos = pointer.pos()
                .offset(pointer.state().get(DispenserBlock.FACING));
            BlockState state = pointer.world().getBlockState(pos);

            if (state.getBlock() instanceof AbstractCauldronBlock cauldronBlock && stack.getCount() == 1) {
                Fluid fluid;

                if (cauldronBlock.isFull(state) && stack.isOf(Items.BUCKET)) {
                    if (state.getBlock() instanceof LavaCauldronBlock) {
                        fluid = Fluids.LAVA;
                    } else {
                        fluid = Fluids.WATER;
                    }

                    pointer.world()
                        .setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                    pointer.world().playSound(
                        null,
                        pos,
                        fluid.getBucketFillSound()
                            .orElse(SoundEvents.ITEM_BUCKET_FILL),
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.5f
                    );

                    return fluid.getBucketItem().getDefaultStack();
                } else {
                    fluid = ((BucketItem) stack.getItem()).getFluid();
                    SoundEvent soundEvent = fluid == Fluids.LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
                    BlockState cauldronState = fluid == Fluids.LAVA ?
                        Blocks.LAVA_CAULDRON.getDefaultState() :
                        Blocks.WATER_CAULDRON.getDefaultState()
                            .with(LeveledCauldronBlock.LEVEL, LeveledCauldronBlock.MAX_LEVEL);

                    pointer.world().setBlockState(pos, cauldronState);
                    pointer.world().playSound(
                        null,
                        pos,
                        soundEvent,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.5f
                    );

                    return Items.BUCKET.getDefaultStack();
                }
            }

            return oldBucketBehavior.dispense(pointer, stack);
        };
        DispenserBlock.registerBehavior(Items.BUCKET, bucketBehavior);
        DispenserBlock.registerBehavior(Items.LAVA_BUCKET, bucketBehavior);
        DispenserBlock.registerBehavior(Items.WATER_BUCKET, bucketBehavior);

        PayloadTypeRegistry.configurationS2C().register(RequestVersionPayload.ID, RequestVersionPayload.PACKET_CODEC);
        PayloadTypeRegistry.configurationC2S().register(VersionResponsePayload.ID, VersionResponsePayload.PACKET_CODEC);

        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, RequestVersionPayload.ID)) {
                handler.addTask(new CheckVersionTask());
            } else {
                disconnect(handler, "unknown");
            }
        });

        ServerConfigurationNetworking.registerGlobalReceiver(VersionResponsePayload.ID, (payload, context) -> {
            if (!CURRENT_VERSION.equals(payload.version())) {
                disconnect(context.networkHandler(), payload.version());
                return;
            }

            context.networkHandler().completeTask(CheckVersionTask.KEY);
        });

        EndermanParalyzeEvent.EVENT.register((this::getIsParalyzed));

        LootTableEvents.MODIFY_DROPS.register(((registryEntry, lootContext, list) -> {
            if (Blocks.HOPPER.getLootTableKey().orElseThrow().equals(registryEntry.getKey().orElseThrow()) && ((SpeedyHopperAccess)lootContext.get(LootContextParameters.BLOCK_ENTITY)).fabricHoliday$isSpeedy()) {
                list.removeIf(stack -> stack.getItem() == Items.HOPPER);
                var compound = new NbtCompound();
                compound.putBoolean("Speedy", true);
                var stack = Items.HOPPER.getDefaultStack().copy();
                stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(BlockEntityType.HOPPER, compound));
                stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                list.add(stack);
            }
        }));
    }

    private static void disconnect(ServerConfigurationNetworkHandler handler, String currentVersion) {
        MutableText text = Text.literal("You must have the same version of the modpack installed to play on this server.");
        text.append("\n").append(Text.literal("Download the following version: ")).append(Text.literal(CURRENT_VERSION).formatted(Formatting.YELLOW));
        text.append("\n").append(Text.literal("You currently have version: ")).append(Text.literal(currentVersion).formatted(Formatting.RED));
        handler.disconnect(new DisconnectionInfo(
            text,
            Optional.empty(),
            Optional.of(URI.create("https://github.com/modmuss50/holiday-server-pack/commit/%s".formatted(CURRENT_VERSION)))
        ));
    }

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public record CheckVersionTask() implements ServerPlayerConfigurationTask {
        public static final Key KEY = new Key(RequestVersionPayload.ID.toString());

        @Override
        public void sendPacket(Consumer<Packet<?>> sender) {
            sender.accept(ServerConfigurationNetworking.createS2CPacket(new RequestVersionPayload()));
        }

        @Override
        public Key getKey() {
            return KEY;
        }
    }
    
    public record RequestVersionPayload() implements CustomPayload {
        public static final CustomPayload.Id<RequestVersionPayload> ID = new CustomPayload.Id<>(Identifier.of("holiday-server-mod", "request_version"));
        public static final PacketCodec<PacketByteBuf, RequestVersionPayload> PACKET_CODEC = PacketCodec.unit(new RequestVersionPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record VersionResponsePayload(String version) implements CustomPayload {
        public static final CustomPayload.Id<VersionResponsePayload> ID = new CustomPayload.Id<>(Identifier.of("holiday-server-mod", "version_response"));
        public static final PacketCodec<PacketByteBuf, VersionResponsePayload> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, VersionResponsePayload::version,
            VersionResponsePayload::new
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public boolean getIsParalyzed(LivingEntity entity) {
        Box box = entity.getBoundingBox().expand(8.0D, 8.0D, 8.0D);
        int n = MathHelper.floor(box.minX);
        int o = MathHelper.floor(box.maxX);
        int p = MathHelper.floor(box.minY);
        int q = MathHelper.floor(box.maxY);
        int n1 = MathHelper.floor(box.minZ);
        int o1 = MathHelper.floor(box.maxZ);
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int p1 = n; p1 < o; p1++)
            for (int q1 = p; q1 < q; q1++)
                for (int n2 = n1; n2 < o1; n2++) {
                    BlockState state = entity.getEntityWorld().getBlockState(mutablePos.set(p1, q1, n2));
                    if (state.getBlock().equals(HolidayServerBlocks.ENDER_PARALYZER)) { //Set the custom block here
                        return true;
                    }
                }
        return false;
    }
}
