package ovh.corail.tombstone.api.magic;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import ovh.corail.tombstone.api.TombstoneAPIProps;

import javax.annotation.Nullable;

public class TBSoulConsumerProvider implements ICapabilityProvider {
    @CapabilityInject(ISoulConsumer.class)
    public static final Capability<ISoulConsumer> CAP_SOUL_CONSUMER = TombstoneAPIProps.unsafeNullCast();
    private final LazyOptional<ISoulConsumer> defaultCap;

    public TBSoulConsumerProvider(ISoulConsumer soulConsumer) {
        this.defaultCap = LazyOptional.of(() -> soulConsumer);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return CAP_SOUL_CONSUMER.orEmpty(cap, this.defaultCap);
    }

    public static ISoulConsumer getDefault() {
        return new ISoulConsumer() {
            @Override
            public boolean isEnchanted(ItemStack stack) {
                return false;
            }

            @Override
            public boolean setEnchant(World world, BlockPos gravePos, PlayerEntity player, ItemStack stack) {
                return false;
            }

            @Override
            public ITextComponent getEnchantSuccessMessage(PlayerEntity player) {
                return new TranslationTextComponent("tombstone.message.enchant_item.success");
            }

            @Override
            public ITextComponent getEnchantFailedMessage(PlayerEntity player) {
                return new TranslationTextComponent("tombstone.message.enchant_item.failed");
            }
        };
    }
}
