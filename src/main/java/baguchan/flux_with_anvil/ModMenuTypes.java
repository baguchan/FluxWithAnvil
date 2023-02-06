package baguchan.flux_with_anvil;

import baguchan.flux_with_anvil.menu.RevampAnvilMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
	public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, FluxWithAnvil.MODID);


	public static final RegistryObject<MenuType<RevampAnvilMenu>> REVAMPED_ANVIL = MENU_TYPES.register("revamped_anvil", () -> new MenuType<>(RevampAnvilMenu::new));
}