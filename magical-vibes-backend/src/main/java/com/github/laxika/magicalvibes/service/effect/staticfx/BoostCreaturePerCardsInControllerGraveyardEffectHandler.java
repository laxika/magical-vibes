package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BoostCreaturePerCardsInControllerGraveyardEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BoostCreaturePerCardsInControllerGraveyardEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var boost = (BoostCreaturePerCardsInControllerGraveyardEffect) effect;
        if (!support.matchesCreatureScope(context, boost.scope(), null)) {
            return;
        }

        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (controllerId == null) return;
        List<Card> graveyard = context.gameData().playerGraveyards.get(controllerId);
        int count = 0;
        if (graveyard != null) {
            for (Card card : graveyard) {
                if (card.isToken()) continue;
                if (gameQueryService.matchesCardPredicate(card, boost.filter(), null)) {
                    count++;
                }
            }
        }
        accumulator.addPower(count * boost.powerPerCard());
        accumulator.addToughness(count * boost.toughnessPerCard());
    }
}
