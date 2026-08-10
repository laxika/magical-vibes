package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardCardsAndPutCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardCardsAndPutCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardCardsAndPutCountersOnSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        List<Integer> validIndices = matchingHandIndices(gameData, controllerId, e, entry.getCard().getId());

        if (gameData.chosenXValue != null) {
            int chosenCount = Math.min(gameData.chosenXValue, validIndices.size());
            gameData.chosenXValue = null;
            if (chosenCount == 0) {
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                        + " chooses to discard 0 " + e.cardDescription() + " for " + cardName + "."));
                return;
            }

            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                    cardName + " — Choose " + e.cardDescription() + " to discard.", chosenCount,
                    DiscardFollowUp.plusOnePlusOneCounters(entry.getSourcePermanentId(),
                            chosenCount * e.countersPerCard()));
            return;
        }

        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                    + " has no " + e.cardDescription() + " to discard for " + cardName + "."));
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                controllerId, validIndices.size(),
                "Choose how many " + e.cardDescription() + " to discard for " + cardName + ".",
                cardName));
        log.info("Game {} - {} chooses how many {} to discard for {}",
                gameData.id, gameData.playerIdToName.get(controllerId), e.cardDescription(), cardName);
    }

    private List<Integer> matchingHandIndices(GameData gameData, UUID controllerId,
                                               DiscardCardsAndPutCountersOnSourceEffect effect,
                                               UUID sourceCardId) {
        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand == null) {
            return validIndices;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(hand.get(i), effect.cardFilter(), sourceCardId)) {
                validIndices.add(i);
            }
        }
        return validIndices;
    }
}
