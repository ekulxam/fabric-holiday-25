package holiday.loot;

import java.util.function.Consumer;

import holiday.CommonEntrypoint;
import holiday.mixin.LootContextTypesAccessor;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextType;

public final class HolidayServerLootContextTypes {
    public static final ContextType BURN_OUT = register("burn_out", builder -> builder
            .require(LootContextParameters.ORIGIN)
            .require(LootContextParameters.BLOCK_STATE)
    );

    private HolidayServerLootContextTypes() {
    }

    public static void register() {
        return;
    }

    public static ContextType register(String path, Consumer<ContextType.Builder> consumer) {
        ContextType.Builder builder = new ContextType.Builder();
        consumer.accept(builder);

        Identifier id = CommonEntrypoint.identifier(path);
        ContextType type = builder.build();

        LootContextTypesAccessor.getMAP().put(id, type);
        return type;
    }
}
