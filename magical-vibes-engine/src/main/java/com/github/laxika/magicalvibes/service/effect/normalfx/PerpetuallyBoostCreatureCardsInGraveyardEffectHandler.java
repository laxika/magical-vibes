package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PerpetuallyBoostCreatureCardsInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostCreatureCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostCreatureCardsInGraveyardEffect) effect;
        for (Card card : gameData.playerGraveyards.getOrDefault(entry.getControllerId(), List.of())) {
            if (card != null && gameQueryService.cardHasType(
                    card, CardType.CREATURE, gameData, entry.getControllerId())) {
                gameData.perpetualPowerToughnessModifiers.merge(
                        card.getId(),
                        new PerpetualPowerToughnessModifier(boost.powerBoost(), boost.toughnessBoost()),
                        PerpetualPowerToughnessModifier::add);
            }
        }
    }
}
