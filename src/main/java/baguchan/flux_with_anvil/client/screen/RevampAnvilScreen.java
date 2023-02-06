package baguchan.flux_with_anvil.client.screen;

import baguchan.flux_with_anvil.FluxWithAnvil;
import baguchan.flux_with_anvil.menu.RevampAnvilMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RevampAnvilScreen extends AbstractContainerScreen<RevampAnvilMenu> implements ContainerListener {
	private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation(FluxWithAnvil.MODID, "textures/gui/container/anvil.png");
	private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");
	private EditBox name;
	private final Player player;

	public RevampAnvilScreen(RevampAnvilMenu p_97874_, Inventory p_97875_, Component p_97876_) {
		super(p_97874_, p_97875_, p_97876_);
		this.player = p_97875_.player;
		this.titleLabelX = 60;
	}

	public void containerTick() {
		super.containerTick();
		this.name.tick();
	}

	protected void init() {
		super.init();
		this.subInit();
		this.menu.addSlotListener(this);
	}

	public void render(PoseStack p_98922_, int p_98923_, int p_98924_, float p_98925_) {
		this.renderBackground(p_98922_);
		super.render(p_98922_, p_98923_, p_98924_, p_98925_);
		RenderSystem.disableBlend();
		this.renderFg(p_98922_, p_98923_, p_98924_, p_98925_);
		this.renderTooltip(p_98922_, p_98923_, p_98924_);
	}

	protected void renderBg(PoseStack p_98917_, float p_98918_, int p_98919_, int p_98920_) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, ANVIL_LOCATION);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(p_98917_, i, j, 0, 0, this.imageWidth, this.imageHeight);
		this.blit(p_98917_, i + 59, j + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
		if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(3).hasItem()) {
			this.blit(p_98917_, i + 99, j + 45, this.imageWidth, 0, 28, 21);
		}

	}

	protected void subInit() {
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, Component.translatable("container.repair"));
		this.name.setCanLoseFocus(false);
		this.name.setTextColor(-1);
		this.name.setTextColorUneditable(-1);
		this.name.setBordered(false);
		this.name.setMaxLength(50);
		this.name.setResponder(this::onNameChanged);
		this.name.setValue("");
		this.addWidget(this.name);
		this.setInitialFocus(this.name);
		this.name.setEditable(false);
	}

	public void resize(Minecraft p_97886_, int p_97887_, int p_97888_) {
		String s = this.name.getValue();
		this.init(p_97886_, p_97887_, p_97888_);
		this.name.setValue(s);
	}

	public void removed() {
		super.removed();
		this.menu.removeSlotListener(this);
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
	}

	public boolean keyPressed(int p_97878_, int p_97879_, int p_97880_) {
		if (p_97878_ == 256) {
			this.minecraft.player.closeContainer();
		}

		return !this.name.keyPressed(p_97878_, p_97879_, p_97880_) && !this.name.canConsumeInput() ? super.keyPressed(p_97878_, p_97879_, p_97880_) : true;
	}

	private void onNameChanged(String p_97899_) {
		if (!p_97899_.isEmpty()) {
			String s = p_97899_;
			Slot slot = this.menu.getSlot(0);
			if (slot != null && slot.hasItem() && !slot.getItem().hasCustomHoverName() && p_97899_.equals(slot.getItem().getHoverName().getString())) {
				s = "";
			}

			this.menu.setItemName(s);
			this.minecraft.player.connection.send(new ServerboundRenameItemPacket(s));
		}
	}

	protected void renderLabels(PoseStack p_97890_, int p_97891_, int p_97892_) {
		RenderSystem.disableBlend();
		super.renderLabels(p_97890_, p_97891_, p_97892_);
		int i = this.menu.getCost();
		if (i > 0) {
			int j = 8453920;
			Component component;
			if (i >= 40 && !this.minecraft.player.getAbilities().instabuild) {
				component = TOO_EXPENSIVE_TEXT;
				j = 16736352;
			} else if (!this.menu.getSlot(3).hasItem()) {
				component = null;
			} else {
				component = Component.translatable("container.flux_with_anvil.repair.cost", i);
				if (!this.menu.getSlot(3).mayPickup(this.player)) {
					j = 16736352;
				}
			}

			if (component != null) {
				int k = this.imageWidth - 8 - this.font.width(component) - 2;
				int l = 69;
				fill(p_97890_, k - 2, 67, this.imageWidth - 8, 79, 1325400064);
				this.font.drawShadow(p_97890_, component, (float)k, 69.0F, j);
			}
		}

	}

	public void renderFg(PoseStack p_97894_, int p_97895_, int p_97896_, float p_97897_) {
		this.name.render(p_97894_, p_97895_, p_97896_, p_97897_);
	}


	public void dataChanged(AbstractContainerMenu p_169759_, int p_169760_, int p_169761_) {
	}

	public void slotChanged(AbstractContainerMenu p_97882_, int p_97883_, ItemStack p_97884_) {
		if (p_97883_ == 0) {
			this.name.setValue(p_97884_.isEmpty() ? "" : p_97884_.getHoverName().getString());
			this.name.setEditable(!p_97884_.isEmpty());
			this.setFocused(this.name);
		}

	}
}