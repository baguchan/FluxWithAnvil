package baguchan.flux_with_anvil.menu;

import baguchan.flux_with_anvil.EventHooks;
import baguchan.flux_with_anvil.FluxConfigs;
import baguchan.flux_with_anvil.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Map;

public class RevampAnvilMenu extends AbstractContainerMenu {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DEBUG_COST = false;
	public static final int MAX_NAME_LENGTH = 50;
	public int repairItemCountCost;
	private String itemName;
	private final DataSlot cost = DataSlot.standalone();
	private final DataSlot fluxCost = DataSlot.standalone();
	private static final int COST_FAIL = 0;
	private static final int COST_BASE = 1;
	private static final int COST_ADDED_BASE = 1;
	private static final int COST_REPAIR_MATERIAL = 1;
	private static final int COST_REPAIR_SACRIFICE = 2;
	private static final int COST_INCOMPATIBLE_PENALTY = 1;
	private static final int COST_RENAME = 1;

	protected final ResultContainer resultSlots = new ResultContainer();
	protected final Container inputSlots = new SimpleContainer(3) {
		public void setChanged() {
			super.setChanged();
			RevampAnvilMenu.this.slotsChanged(this);
		}
	};
	protected final ContainerLevelAccess access;
	protected final Player player;

	public RevampAnvilMenu(int p_40245_, Inventory p_40246_) {
		this(p_40245_, p_40246_, ContainerLevelAccess.NULL);
	}

	public RevampAnvilMenu(int p_40248_, Inventory p_40249_, ContainerLevelAccess p_40250_) {
		super(ModMenuTypes.REVAMPED_ANVIL.get(), p_40248_);
		this.access = p_40250_;
		this.player = p_40249_.player;
		this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
		this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
		this.addSlot(new Slot(this.inputSlots, 2, 52, 65){
			@Override
			public boolean mayPlace(ItemStack p_40231_) {
				return FluxConfigs.isWhitelistedItem(p_40231_.getItem());
			}
		});
		this.addSlot(new Slot(this.resultSlots, 3, 134, 47) {
			public boolean mayPlace(ItemStack p_39818_) {
				return RevampAnvilMenu.this.mayPickup(player, !this.hasItem());
			}

			public void onTake(Player p_150604_, ItemStack p_150605_) {
				RevampAnvilMenu.this.onTake(p_150604_, p_150605_);
			}
		});

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(p_40249_, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(p_40249_, k, 8 + k * 18, 142));
		}

		this.addDataSlot(this.cost);
		this.addDataSlot(this.fluxCost);
	}

	public void slotsChanged(Container p_39778_) {
		super.slotsChanged(p_39778_);
		if (p_39778_ == this.inputSlots) {
			this.createResult();
		}

	}

	public void removed(Player p_39790_) {
		super.removed(p_39790_);
		this.access.execute((p_39796_, p_39797_) -> {
			this.clearContainer(p_39790_, this.inputSlots);
		});
	}

	public boolean stillValid(Player p_39780_) {
		return this.access.evaluate((p_39785_, p_39786_) -> {
			return !this.isValidBlock(p_39785_.getBlockState(p_39786_)) ? false : p_39780_.distanceToSqr((double) p_39786_.getX() + 0.5D, (double) p_39786_.getY() + 0.5D, (double) p_39786_.getZ() + 0.5D) <= 64.0D;
		}, true);
	}

	protected boolean shouldQuickMoveToAdditionalSlot(ItemStack p_39787_) {
		return false;
	}

	public ItemStack quickMoveStack(Player p_39792_, int p_39793_) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(p_39793_);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (p_39793_ == 3) {
				if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(itemstack1, itemstack);
			} else if (p_39793_ != 0 && p_39793_ != 1 && p_39793_ != 2) {
				if (p_39793_ >= 4 && p_39793_ < 40) {
					int i = this.shouldQuickMoveToAdditionalSlot(itemstack) ? 1 : 0;
					if (!this.moveItemStackTo(itemstack1, i, 3, false)) {
						return ItemStack.EMPTY;
					}
				}
			} else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(p_39792_, itemstack1);
		}

		return itemstack;
	}
	protected boolean isValidBlock(BlockState p_39019_) {
		return p_39019_.is(BlockTags.ANVIL);
	}

	protected boolean mayPickup(Player p_39023_, boolean p_39024_) {
		return (p_39023_.getAbilities().instabuild || p_39023_.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
	}

	protected void onTake(Player p_150474_, ItemStack p_150475_) {
		ItemStack itemstack2 = this.inputSlots.getItem(2);
		itemstack2.shrink(this.fluxCost.get());
		if (!p_150474_.getAbilities().instabuild) {

			p_150474_.giveExperienceLevels(-this.cost.get());
		}

		float breakChance = net.minecraftforge.common.ForgeHooks.onAnvilRepair(p_150474_, p_150475_, RevampAnvilMenu.this.inputSlots.getItem(0), RevampAnvilMenu.this.inputSlots.getItem(1));

		this.inputSlots.setItem(0, ItemStack.EMPTY);
		if (this.repairItemCountCost > 0) {
			ItemStack itemstack = this.inputSlots.getItem(1);
			if (!itemstack.isEmpty() && itemstack.getCount() > this.repairItemCountCost) {
				itemstack.shrink(this.repairItemCountCost);
				this.inputSlots.setItem(1, itemstack);
			} else {
				this.inputSlots.setItem(1, ItemStack.EMPTY);
			}
		} else {
			this.inputSlots.setItem(1, ItemStack.EMPTY);
		}

		this.cost.set(0);
		this.access.execute((p_150479_, p_150480_) -> {
			BlockState blockstate = p_150479_.getBlockState(p_150480_);
			if (!p_150474_.getAbilities().instabuild && blockstate.is(BlockTags.ANVIL) && p_150474_.getRandom().nextFloat() < breakChance) {
				BlockState blockstate1 = AnvilBlock.damage(blockstate);
				if (blockstate1 == null) {
					p_150479_.removeBlock(p_150480_, false);
					p_150479_.levelEvent(1029, p_150480_, 0);
				} else {
					p_150479_.setBlock(p_150480_, blockstate1, 2);
					p_150479_.levelEvent(1030, p_150480_, 0);
				}
			} else {
				p_150479_.levelEvent(1030, p_150480_, 0);
			}

		});
	}

	public void createResult() {
		ItemStack itemstack = this.inputSlots.getItem(0);
		ItemStack flux = this.inputSlots.getItem(2);
		this.cost.set(1);
		int i = 0;
		int j = 0;
		int k = 0;
		if (itemstack.isEmpty()) {
			this.resultSlots.setItem(0, ItemStack.EMPTY);
			this.cost.set(0);
			this.fluxCost.set(0);
		} else {
			ItemStack itemstack1 = itemstack.copy();
			ItemStack itemstack2 = this.inputSlots.getItem(1);
			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
			j += itemstack.getBaseRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getBaseRepairCost());
			this.repairItemCountCost = 0;
			boolean flag = false;
			boolean enchantFlag = false;
			boolean enchantIncreaseFlag = false;

			if (!EventHooks.onAnvilChange(this, itemstack, itemstack2, resultSlots, itemName, j, this.player)) return;
			/*this.resultSlots.setItem(0, e.getOutput());
			this.setMaximumCost(j);
			container.setMaximumCost(e.getCost());
			container.repairItemCountCost = e.getMaterialCost();*/

			if (!itemstack2.isEmpty()) {
				flag = itemstack2.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemstack2).isEmpty();
				if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2)) {
					int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
					if (l2 <= 0) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						this.fluxCost.set(0);
						return;
					}

					int i3;
					for(i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3) {
						int j3 = itemstack1.getDamageValue() - l2;
						itemstack1.setDamageValue(j3);
						++i;
						l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
					}

					this.repairItemCountCost = i3;
				} else {
					if (!flag && (!itemstack1.is(itemstack2.getItem()) || !itemstack1.isDamageableItem())) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						this.fluxCost.set(0);
						return;
					}

					if (itemstack1.isDamageableItem() && !flag) {
						int l = itemstack.getMaxDamage() - itemstack.getDamageValue();
						int i1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
						int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
						int k1 = l + j1;
						int l1 = itemstack1.getMaxDamage() - k1;
						if (l1 < 0) {
							l1 = 0;
						}

						if (l1 < itemstack1.getDamageValue()) {
							itemstack1.setDamageValue(l1);
							i += 2;
						}
					}

					Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
					boolean flag2 = false;
					boolean flag3 = false;

					for(Enchantment enchantment1 : map1.keySet()) {
						if (enchantment1 != null) {
							int i2 = map.getOrDefault(enchantment1, 0);
							int j2 = map1.get(enchantment1);
							enchantIncreaseFlag = i2 == j2;
							j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);

							boolean flag1 = enchantment1.canEnchant(itemstack);
							if (this.player.getAbilities().instabuild || itemstack.is(Items.ENCHANTED_BOOK)) {
								flag1 = true;
							}

							for(Enchantment enchantment : map.keySet()) {
								if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
									flag1 = false;
									++i;
								}
							}

							if (!flag1) {
								flag3 = true;
							} else {
								flag2 = true;
								if (j2 > enchantment1.getMaxLevel()) {
									j2 = enchantment1.getMaxLevel();
								}

								map.put(enchantment1, j2);
								int k3 = 0;
								switch (enchantment1.getRarity()) {
									case COMMON:
										k3 = 1;
										break;
									case UNCOMMON:
										k3 = 2;
										break;
									case RARE:
										k3 = 4;
										break;
									case VERY_RARE:
										k3 = 8;
								}

								if (flag) {
									k3 = Math.max(1, k3 / 2);
								}

								i += k3 * j2;
								if (itemstack.getCount() > 1) {
									i = 40;
								}
								enchantFlag = true;
							}
						}
					}

					if (flag3 && !flag2) {
						this.resultSlots.setItem(0, ItemStack.EMPTY);
						this.cost.set(0);
						this.fluxCost.set(0);
						return;
					}
				}
			}

			boolean nameFlag = false;


			if (StringUtils.isBlank(this.itemName)) {
				if (itemstack.hasCustomHoverName()) {
					k = 1;
					i += k;
					itemstack1.resetHoverName();
				}
			} else if (!this.itemName.equals(itemstack.getHoverName().getString())) {
				k = 1;
				i += k;
				itemstack1.setHoverName(Component.literal(this.itemName));
				nameFlag = true;
			}
			if (flag && !itemstack1.isBookEnchantable(itemstack2)) itemstack1 = ItemStack.EMPTY;

			this.cost.set(j + i);
			if (i <= 0) {
				itemstack1 = ItemStack.EMPTY;
			}

			if (k == i && k > 0 && this.cost.get() >= 40) {
				this.cost.set(39);
			}

			if (this.cost.get() >= 40 && !this.player.getAbilities().instabuild) {
				itemstack1 = ItemStack.EMPTY;
			}
			int k2 = 0;
			if (!itemstack1.isEmpty()) {
				k2 = itemstack1.getBaseRepairCost();
				if (!itemstack2.isEmpty() && k2 < itemstack2.getBaseRepairCost()) {
					k2 = itemstack2.getBaseRepairCost();
				}

				if (k != i || k == 0) {
					k2 = calculateIncreasedRepairCost(k2);
				}

				itemstack1.setRepairCost(k2);
				EnchantmentHelper.setEnchantments(map, itemstack1);
			}

			/*if(enchantIncreaseFlag){
				this.setFluxCost(k2);
			}else {
				this.setFluxCost(calculateFluxCost(k2));
			}*/
			if(nameFlag && !enchantFlag){
				this.setFluxCost(0);
			}else {
				if(FluxConfigs.COMMON.noFluxUsingWithEnchantBooks.get() && flag) {
					this.setFluxCost(0);
				}else {
					this.setFluxCost(this.getCost());
				}
			}

			if(FluxConfigs.isWhitelistedItem(flux.getItem()) && flux.getCount() >= this.getFluxCost() || nameFlag && !enchantFlag) {
				this.resultSlots.setItem(0, itemstack1);
			}else {
				this.resultSlots.setItem(0, ItemStack.EMPTY);
			}
			this.broadcastChanges();
		}
	}

	public static int calculateIncreasedRepairCost(int p_39026_) {
		return p_39026_ * 2 + 1;
	}

	public static int calculateFluxCost(int p_39026_) {
		return Mth.clamp((p_39026_ / 10) + 1, 1, 3);
	}

	public void setItemName(String p_39021_) {
		this.itemName = p_39021_;
		if (this.getSlot(3).hasItem()) {
			ItemStack itemstack = this.getSlot(3).getItem();
			if (StringUtils.isBlank(p_39021_)) {
				itemstack.resetHoverName();
			} else {
				itemstack.setHoverName(Component.literal(this.itemName));
			}
		}

		this.createResult();
	}

	public int getCost() {
		return this.cost.get();
	}

	public void setMaximumCost(int value) {
		this.cost.set(value);
	}

	public int getFluxCost() {
		return this.fluxCost.get();
	}

	public void setFluxCost(int value) {
		this.fluxCost.set(value);
	}
}