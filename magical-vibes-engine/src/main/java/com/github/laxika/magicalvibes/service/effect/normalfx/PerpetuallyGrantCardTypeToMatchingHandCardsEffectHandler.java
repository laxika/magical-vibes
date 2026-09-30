package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardTypeToMatchingHandCardsEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves perpetual card-type grants to matching cards currently in hand. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantCardTypeToMatchingHandCardsEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantCardTypeToMatchingHandCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantCardTypeToMatchingHandCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        for (Card card : hand) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, grant.filter(), null, gameData, controllerId)) {
                continue;
            }
            gameData.perpetualCardTypes.merge(card.getId(), Set.of(grant.cardType()),
                    (existing, added) -> {
                        Set<CardType> merged = EnumSet.noneOf(CardType.class);
                        merged.addAll(existing);
                        merged.addAll(added);
                        return Set.copyOf(merged);
                    });
        }
    }
}
