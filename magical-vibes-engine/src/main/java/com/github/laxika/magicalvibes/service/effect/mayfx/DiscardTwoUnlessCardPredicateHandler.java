package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardTwoUnlessCardPredicateEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiscardTwoUnlessCardPredicateHandler implements MayEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final InputCompletionService inputCompletionService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardTwoUnlessCardPredicateEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        DiscardTwoUnlessCardPredicateEffect discardEffect = ability.effects().stream()
                .filter(DiscardTwoUnlessCardPredicateEffect.class::isInstance)
                .map(DiscardTwoUnlessCardPredicateEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID controllerId = ability.controllerId();
        if (accepted) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            List<Integer> matchingIndices = new ArrayList<>();
            if (hand != null) {
                for (int i = 0; i < hand.size(); i++) {
                    if (predicateEvaluationService.matchesCardPredicate(
                            hand.get(i), discardEffect.predicate(), null)) {
                        matchingIndices.add(i);
                    }
                }
            }

            if (!matchingIndices.isEmpty()) {
                gameData.discardCausedByOpponent = false;
                playerInputService.beginDiscardChoice(
                        gameData, controllerId, matchingIndices,
                        "Choose a matching card to discard.", 1);
                return;
            }
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 2);
    }
}
