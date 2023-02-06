package baguchan.flux_with_anvil.mixin;

import baguchan.flux_with_anvil.menu.RevampAnvilMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilBlock.class)
public class AnvilBlockMixin {
	@Shadow
	@Final
	private static Component CONTAINER_TITLE;


	@Inject(method = "getMenuProvider", at = @At("HEAD"), cancellable = true)
	public void getMenuProvider(BlockState p_48821_, Level p_48822_, BlockPos p_48823_, CallbackInfoReturnable<MenuProvider> callbackInfoReturnable) {
		callbackInfoReturnable.setReturnValue(new SimpleMenuProvider((p_48785_, p_48786_, p_48787_) -> {
			return new RevampAnvilMenu(p_48785_, p_48786_, ContainerLevelAccess.create(p_48822_, p_48823_));
		}, CONTAINER_TITLE));
	}
}
