package com.buuz135.industrial.block.transportstorage;

import com.buuz135.industrial.block.IndustrialBlock;
import com.buuz135.industrial.block.transportstorage.tile.BlackHoleTankTile;
import com.buuz135.industrial.capability.BlockFluidHandlerItemStack;
import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.module.ModuleTransportStorage;
import com.buuz135.industrial.utils.BlockUtils;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.datagenerator.loot.block.BasicBlockLootTables;
import com.hrznstudio.titanium.module.api.RegistryManager;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.util.LangUtil;
import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;

public class BlackHoleTankBlock extends IndustrialBlock<BlackHoleTankTile> {

    private Rarity rarity;
    private TileEntityType tileEntityType;

    public BlackHoleTankBlock(Rarity rarity) {
        super(rarity.name() + "_black_hole_tank",  Properties.from(Blocks.IRON_BLOCK), BlackHoleTankTile.class, ModuleTransportStorage.TAB_TRANSPORT);
        this.rarity = rarity;
    }

    @Override
    public IFactory<BlackHoleTankTile> getTileEntityFactory() {
        return () -> new BlackHoleTankTile(this, rarity);
    }

    @Override
    public void addAlternatives(RegistryManager<?> registry) {
        BlockItem item = this.getItemBlockFactory().create();
        setItem(item);
        registry.content(Item.class, item);
        NBTManager.getInstance().scanTileClassForAnnotations(BlackHoleTankTile.class);
        tileEntityType = TileEntityType.Builder.create(this.getTileEntityFactory()::create, new Block[]{this}).build((Type) null);
        tileEntityType.setRegistryName(new ResourceLocation(Reference.MOD_ID, rarity.name() + "_black_hole_tank"));
        registry.content(TileEntityType.class, tileEntityType);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(iFluidHandlerItem -> {
            if (!iFluidHandlerItem.getFluidInTank(0).isEmpty()){
                tooltip.add(new StringTextComponent(TextFormatting.GOLD + LangUtil.getString("text.industrialforegoing.tooltip.contains") +": " + TextFormatting.WHITE + new DecimalFormat().format(iFluidHandlerItem.getFluidInTank(0).getAmount()) + TextFormatting.YELLOW + LangUtil.getString("tooltip.industrialforegoing.mb_of",TextFormatting.DARK_AQUA+ iFluidHandlerItem.getFluidInTank(0).getDisplayName().getString())));
            }
        });
        tooltip.add(new StringTextComponent(TextFormatting.GOLD + LangUtil.getString("text.industrialforegoing.tooltip.can_hold") + ": " + TextFormatting.WHITE+ new DecimalFormat().format(BlockUtils.getFluidAmountByRarity(rarity)) + TextFormatting.DARK_AQUA + LangUtil.getString("text.industrialforegoing.tooltip.mb")));
    }

    @Override
    public IFactory<BlockItem> getItemBlockFactory() {
        return () -> (BlockItem) new BlackHoleTankItem(this, new Item.Properties().group(this.getItemGroup()), rarity).setRegistryName(this.getRegistryName());
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public TileEntityType getTileEntityType() {
        return tileEntityType;
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
        //getTile(worldIn, pos).ifPresent(tile -> tile.onClicked(player));
    }

    @Override
    public LootTable.Builder getLootTable(@Nonnull BasicBlockLootTables blockLootTables) {
        CopyNbt.Builder nbtBuilder = CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY);
        nbtBuilder.replaceOperation("tank",  "BlockEntityTag.tank");
        nbtBuilder.replaceOperation("filter",  "BlockEntityTag.filter");
        return blockLootTables.droppingSelfWithNbt(this, nbtBuilder);
    }

    @Override
    public NonNullList<ItemStack> getDynamicDrops(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        return NonNullList.create();
    }

    @Override
    public void registerRecipe(Consumer<IFinishedRecipe> consumer) {
        ITag tag = IndustrialTags.Items.MACHINE_FRAME_PITY;
        if (rarity == ModuleCore.SIMPLE_RARITY) tag = IndustrialTags.Items.MACHINE_FRAME_SIMPLE;
        if (rarity == ModuleCore.ADVANCED_RARITY) tag = IndustrialTags.Items.MACHINE_FRAME_ADVANCED;
        if (rarity == ModuleCore.SUPREME_RARITY) tag = IndustrialTags.Items.MACHINE_FRAME_SUPREME;
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .patternLine("PPP").patternLine("NEN").patternLine("CMC")
                .key('P', IndustrialTags.Items.PLASTIC)
                .key('N', Items.ENDER_EYE)
                .key('E', Items.ENDER_PEARL)
                .key('C', Items.BUCKET)
                .key('M', tag)
                .build(consumer);
    }

    public class BlackHoleTankItem extends BlockItem{

        private Rarity rarity;

        public BlackHoleTankItem(Block blockIn, Properties builder, Rarity rarity) {
            super(blockIn, builder);
            this.rarity = rarity;
        }

        @Nullable
        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
            return new BlackHoleTankCapabilityProvider(stack, this.rarity);
        }
    }

    public class BlackHoleTankCapabilityProvider implements ICapabilityProvider {

        private final ItemStack stack;
        private LazyOptional<FluidHandlerItemStack> iFluidHandlerItemLazyOptional;

        public BlackHoleTankCapabilityProvider(ItemStack stack, Rarity rarity) {
            this.stack = stack;
            this.iFluidHandlerItemLazyOptional = LazyOptional.of(() -> new BlockFluidHandlerItemStack(stack, new ItemStack(stack.getItem()), BlockUtils.getFluidAmountByRarity(rarity), "tank"));
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap != null && cap.equals(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)) return iFluidHandlerItemLazyOptional.cast();
            return LazyOptional.empty();
        }
    }
}
