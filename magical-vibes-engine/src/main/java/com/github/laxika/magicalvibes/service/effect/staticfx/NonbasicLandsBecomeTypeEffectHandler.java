package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsBecomeTypeEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

/**
 * Applies {@link NonbasicLandsBecomeTypeEffect}: every nonbasic land on the battlefield takes on
 * the effect's basic land type, losing its other land types (rule 305.7). The intrinsic mana
 * ability of the new type is produced via {@code GameQueryService.getOverriddenLandManaColor}.
 */
@Component
public class NonbasicLandsBecomeTypeEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NonbasicLandsBecomeTypeEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var becomesType = (NonbasicLandsBecomeTypeEffect) effect;
        var card = context.target().getCard();
        if (card.hasType(CardType.LAND) && !card.getSupertypes().contains(CardSupertype.BASIC)) {
            accumulator.addGrantedSubtype(becomesType.subtype());
            accumulator.setSubtypeOverriding(true);
            accumulator.setLandSubtypeOverriding(true);
        }
    }
}
