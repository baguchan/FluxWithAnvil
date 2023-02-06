package baguchan.flux_with_anvil;

import baguchan.flux_with_anvil.menu.RevampAnvilMenu;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import org.jetbrains.annotations.NotNull;

public class EventHooks {
	public static boolean onAnvilChange(RevampAnvilMenu container, @NotNull ItemStack left, @NotNull ItemStack right, Container outputSlot, String name, int baseCost, Player player)
	{
		AnvilUpdateEvent e = new AnvilUpdateEvent(left, right, name, baseCost, player);
		if (MinecraftForge.EVENT_BUS.post(e)) return false;
		if (e.getOutput().isEmpty()) return true;

		outputSlot.setItem(0, e.getOutput());
		container.setMaximumCost(e.getCost());
		container.repairItemCountCost = e.getMaterialCost();
		return false;
	}
}
