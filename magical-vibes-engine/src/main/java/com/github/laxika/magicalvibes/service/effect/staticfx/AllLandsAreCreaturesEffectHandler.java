package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.AllLandsAreCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fills the creature-ness and fixed base P/T of an {@link AllLandsAreCreaturesEffect} (Nature's
 * Revolt) into every land's static bonus. The layer-4 creature type is added by the layered pass;
 * this handler contributes the layer-7 base P/T (the effect's fixed values) for the accumulator and
 * the view. Manlands that animate themselves keep their own base P/T (their self-animation defines
 * it, so it is left untouched here).
 */
@Component
@RequiredArgsConstructor
public class AllLandsAreCreaturesEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllLandsAreCreaturesEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var e = (AllLandsAreCreaturesEffect) effect;
        if (context.target().getCard().hasType(CardType.LAND)
                && (e.requiredSubtype() == null
                        || context.target().getCard().getSubtypes().contains(e.requiredSubtype()))
                && !gameQueryService.hasSelfBecomeCreatureEffect(context.gameData(), context.target())) {
            accumulator.setAnimatedCreature(true);
            accumulator.setBasePTOverride(e.power(), e.toughness());
            accumulator.addGrantedCardType(CardType.CREATURE);
        }
    }
}
