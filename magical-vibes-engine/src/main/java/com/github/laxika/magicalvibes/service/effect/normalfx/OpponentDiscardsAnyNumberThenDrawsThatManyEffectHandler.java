package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentDiscardsAnyNumberThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a non-targeting opponent rummage effect in the two-player engine. */
@Component
@RequiredArgsConstructor
public class OpponentDiscardsAnyNumberThenDrawsThatManyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentDiscardsAnyNumberThenDrawsThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, entry.getControllerId());
        if (opponentId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(opponentId);
        String opponentName = gameData.playerIdToName.get(opponentId);
        String cardName = entry.getCard().getName();

        if (gameData.chosenXValue != null) {
            int chosenCount = Math.max(0, gameData.chosenXValue);
            gameData.chosenXValue = null;
            if (chosenCount == 0 || hand == null || hand.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(opponentName + " chooses to discard 0 cards for " + cardName + "."));
                return;
            }

            int actualCount = Math.min(chosenCount, hand.size());
            gameData.discardCausedByOpponent = true;
            playerInteractionSupport.resolveDiscardCards(gameData, opponentId, actualCount,
                    DiscardFollowUp.rummage(actualCount));
            return;
        }

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(opponentName + " has no cards to discard for " + cardName + "."));
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                opponentId, hand.size(), "Discard any number of cards for " + cardName
                        + ". You will draw that many cards.", cardName));
    }
}
