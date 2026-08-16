package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsUpToThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the targeted up-to discard and draw effect through the shared discard interaction flow.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerDiscardsUpToThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDiscardsUpToThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TargetPlayerDiscardsUpToThenDrawsThatManyEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        if (gameData.chosenXValue != null) {
            int chosenCount = Math.max(0, gameData.chosenXValue);
            gameData.chosenXValue = null;
            if (hand == null || hand.isEmpty() || chosenCount == 0) {
                return;
            }

            int actualCount = Math.min(chosenCount, hand.size());
            gameData.discardCausedByOpponent = !entry.getControllerId().equals(targetPlayerId);
            playerInteractionSupport.resolveDiscardCards(gameData, targetPlayerId, actualCount,
                    DiscardFollowUp.rummage(actualCount));
            return;
        }

        int maxDiscard = Math.max(0, amountEvaluationService.evaluate(
                gameData, e.maxDiscard(), AmountContext.forStackEntry(entry, null)));
        if (maxDiscard == 0 || hand == null || hand.isEmpty()) {
            if (hand == null || hand.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(targetPlayerId) + " has no cards to discard."));
            }
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                targetPlayerId, Math.min(maxDiscard, hand.size()),
                "Discard up to " + maxDiscard + " cards for " + entry.getCard().getName()
                        + ". Draw that many cards.", entry.getCard().getName()));
    }
}
