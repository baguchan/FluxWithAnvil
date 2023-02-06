package baguchan.flux_with_anvil.mixin;

import baguchan.flux_with_anvil.menu.RevampAnvilMenu;
import net.minecraft.SharedConstants;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleRenameItem", at = @At("TAIL"))
	public void handleRenameItem(ServerboundRenameItemPacket p_9899_, CallbackInfo callbackInfo) {
		AbstractContainerMenu $$2 = this.player.containerMenu;
		if ($$2 instanceof RevampAnvilMenu anvilmenu) {
			if (!anvilmenu.stillValid(this.player)) {
				return;
			}

			String s = SharedConstants.filterText(p_9899_.getName());
			if (s.length() <= 50) {
				anvilmenu.setItemName(s);
			}
		}

	}
}
