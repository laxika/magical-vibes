package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Applies {@link ControlledLandsBecomeTypeEffect}: every land the source's controller controls takes
 * on the effect's basic land type, losing its other land types (rule 305.7). The intrinsic mana
 * ability of the new type is produced via {@code GameQueryService.getOverriddenLandManaColor}.
 */
@Component
public class ControlledLandsBecomeTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlledLandsBecomeTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomesType = (ControlledLandsBecomeTypeEffect) effect;
        if (context.targetOnSameBattlefield() && context.target().getCard().hasType(CardType.LAND)) {
            accumulator.addGrantedSubtype(becomesType.subtype());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }
}
