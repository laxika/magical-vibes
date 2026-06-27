package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerMatchingLandNameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoostCreaturePerMatchingLandNameEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturePerMatchingLandNameEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerMatchingLandNameEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        Card imprintedCard = context.source().getCard().getImprintedCard();
        if (imprintedCard == null) {
            return;
        }

        String imprintedName = imprintedCard.getName();
        final int[] count = {0};
        context.gameData().forEachPermanent((playerId, permanent) -> {
            if (permanent.getCard().hasType(CardType.LAND)) {
                if (imprintedName.equals(permanent.getCard().getName())) {
                    count[0]++;
                }
            }
        });

        accumulator.addPower(count[0] * boost.powerPerMatch());
        accumulator.addToughness(count[0] * boost.toughnessPerMatch());
    }
}
