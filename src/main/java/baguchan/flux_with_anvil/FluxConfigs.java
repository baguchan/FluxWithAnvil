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
		public final ForgeConfigSpec.BooleanValue noFluxUsingWithEnchantBooks;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> fluxWhitelist;

		public Common(ForgeConfigSpec.Builder builder) {
			Predicate<Object> validator = o -> o instanceof String;
			noFluxUsingWithEnchantBooks = builder
					.comment("Disable using flux With Enchant Books. [true / false]")
					.define("No Flux Using With Enchant Books", false);
			fluxWhitelist = builder
					.comment("Add Item for Anvil's flux [example: minecraft:calcite]")
					.defineList("Anvil Flux Whitelist"
							, Lists.newArrayList("minecraft:calcite")
							, validator);
		}
	}

	public static boolean isWhitelistedItem(Item item) {
		return FluxConfigs.COMMON.fluxWhitelist.get().contains(ForgeRegistries.ITEMS.getKey(item).toString());
	}

}