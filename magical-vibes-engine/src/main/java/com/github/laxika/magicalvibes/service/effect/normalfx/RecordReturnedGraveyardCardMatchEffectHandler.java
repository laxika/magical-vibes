package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RecordReturnedGraveyardCardMatchEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves {@link RecordReturnedGraveyardCardMatchEffect}. */
@Component
@RequiredArgsConstructor
public class RecordReturnedGraveyardCardMatchEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RecordReturnedGraveyardCardMatchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);

        UUID returnedCardId = entry.getTargetId();
        if (returnedCardId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(entry.getControllerId());
        Card returnedCard = hand == null ? null
                : hand.stream().filter(card -> card.getId().equals(returnedCardId)).findFirst().orElse(null);
        if (returnedCard == null) {
            return;
        }

        RecordReturnedGraveyardCardMatchEffect matchEffect = (RecordReturnedGraveyardCardMatchEffect) effect;
        if (predicateEvaluationService.matchesCardPredicate(
                returnedCard,
                matchEffect.predicate(),
                entry.getCard().getId(),
                gameData,
                entry.getControllerId())) {
            entry.setEventValue(1);
        }
    }
}
