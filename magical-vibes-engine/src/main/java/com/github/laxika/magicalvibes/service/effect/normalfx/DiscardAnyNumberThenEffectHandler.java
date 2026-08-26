package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardAnyNumberThenEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardAnyNumberThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var discardEffect = (DiscardAnyNumberThenEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Integer> validIndices = matchingHandIndices(gameData, controllerId, discardEffect, entry.getCard().getId());

        if (gameData.chosenXValue != null) {
            int chosenCount = Math.min(gameData.chosenXValue, validIndices.size());
            gameData.chosenXValue = null;
            entry.setEventValue(chosenCount);
            if (chosenCount == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " chooses to discard 0 "
                        + discardEffect.cardDescription() + " for " + cardName + "."));
                return;
            }

            gameData.discardCausedByOpponent = false;
            playerInputService.beginDiscardChoice(
                    gameData,
                    controllerId,
                    validIndices,
                    cardName + " — Choose " + discardEffect.cardDescription() + " to discard.",
                    chosenCount,
                    DiscardFollowUp.thenEffectWithEventValue(
                            entry.getCard(),
                            discardEffect.thenEffect(),
                            chosenCount,
                            entry.getSourcePermanentId(),
                            entry.getSourcePermanentSnapshot()));
            return;
        }

        entry.setEventValue(0);
        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no "
                    + discardEffect.cardDescription() + " to discard for " + cardName + "."));
            log.info("Game {} - {} has no {} to discard for {}", gameData.id, playerName,
                    discardEffect.cardDescription(), cardName);
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                controllerId,
                validIndices.size(),
                "Choose how many " + discardEffect.cardDescription() + " to discard for " + cardName + ".",
                cardName));
    }

    private List<Integer> matchingHandIndices(GameData gameData, UUID controllerId,
                                               DiscardAnyNumberThenEffect effect, UUID sourceCardId) {
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
