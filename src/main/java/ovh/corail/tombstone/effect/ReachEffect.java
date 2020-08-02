package ovh.corail.tombstone.effect;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.math.MathHelper;

public class ReachEffect extends Effect {
    private static final String REACH_MODIFIER = "7a29ec0c-86cf-47e1-b204-20c65b89ddc2";

    public ReachEffect() {
        super(EffectType.BENEFICIAL, 0x0);
        addAttributesModifier(PlayerEntity.REACH_DISTANCE, REACH_MODIFIER, 1d, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier) {
        return modifier.getAmount() * MathHelper.clamp(amplifier + 1d, 1d, 10d);
    }
}
