package baguchan.flux_with_anvil;

import com.google.common.collect.Lists;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Predicate;

public class FluxConfigs {
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	static {
		Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}
	public static class Common {
		public final ForgeConfigSpec.BooleanValue noItemUsingWithEnchantBooks;
		public final ForgeConfigSpec.BooleanValue reduceItemCost;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> fluxWhitelist;

		public Common(ForgeConfigSpec.Builder builder) {
			Predicate<Object> validator = o -> o instanceof String;
			noItemUsingWithEnchantBooks = builder
					.comment("Disable using item With Enchant Books on Anvil. [true / false]")
					.define("No Item Using With Enchant Books", false);
			reduceItemCost = builder
					.comment("Reduce the amount of items needed for Anvil. [true / false]")
					.define("ReduceItemCost", false);
			fluxWhitelist = builder
					.comment("Add Item for Anvil's item [example: minecraft:lapis_lazuli]")
					.defineList("Anvil Item Whitelist"
							, Lists.newArrayList("minecraft:lapis_lazuli")
							, validator);
		}
	}

	public static boolean isWhitelistedItem(Item item) {
		return FluxConfigs.COMMON.fluxWhitelist.get().contains(ForgeRegistries.ITEMS.getKey(item).toString());
	}

}