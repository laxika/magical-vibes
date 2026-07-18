package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeBecomeTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Applies {@link LandsOfSubtypeBecomeTypeEffect}: every land carrying the effect's {@code
 * fromSubtype} takes on the {@code toSubtype} basic land type, losing its other land types (rule
 * 305.7). The intrinsic mana ability of the new type is produced via {@code
 * GameQueryService.getOverriddenLandManaColor}.
 */
@Component
public class LandsOfSubtypeBecomeTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LandsOfSubtypeBecomeTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomes = (LandsOfSubtypeBecomeTypeEffect) effect;
        var card = context.target().getCard();
        if (card.hasType(CardType.LAND) && card.getSubtypes().contains(becomes.fromSubtype())) {
            accumulator.addGrantedSubtype(becomes.toSubtype());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }
}
