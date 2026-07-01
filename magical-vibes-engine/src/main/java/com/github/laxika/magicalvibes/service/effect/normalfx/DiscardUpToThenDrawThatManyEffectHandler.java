package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardUpToThenDrawThatManyEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardUpToThenDrawThatManyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardUpToThenDrawThatManyEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        // Re-entry after player chose how many to discard
        if (gameData.chosenXValue != null) {
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenCount == 0) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " chooses to discard 0 cards for " + cardName + ".");
                log.info("Game {} - {} chooses to discard 0 for {}", gameData.id, playerName, cardName);
                if (e.extraDraw() > 0) {
                    playerInteractionSupport.applyDrawCards(gameData, controllerId, e.extraDraw());
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " draws " + e.extraDraw() + " card"
                                    + (e.extraDraw() != 1 ? "s" : "") + ".");
                }
                return;
            }

            // Store the draw count for after discards complete
            gameData.pendingRummageDrawCount = chosenCount + e.extraDraw();
            gameData.discardCausedByOpponent = false;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, chosenCount);
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            if (e.extraDraw() > 0) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " chooses to discard 0 cards for " + cardName + ".");
                playerInteractionSupport.applyDrawCards(gameData, controllerId, e.extraDraw());
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " draws " + e.extraDraw() + " card"
                                + (e.extraDraw() != 1 ? "s" : "") + ".");
                log.info("Game {} - {} discards 0 and draws {} for {}",
                        gameData.id, playerName, e.extraDraw(), cardName);
            } else {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " has no cards to discard for " + cardName + ".");
                log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            }
            return;
        }

        int maxDiscard = e.maxDiscard() == DiscardUpToThenDrawThatManyEffect.ANY_NUMBER
                ? hand.size()
                : Math.min(e.maxDiscard(), hand.size());
        String prompt = buildPrompt(e, cardName);
        playerInputService.beginXValueChoice(gameData, controllerId, maxDiscard, prompt, cardName);
    }

    private static String buildPrompt(DiscardUpToThenDrawThatManyEffect e, String cardName) {
        boolean anyNumber = e.maxDiscard() == DiscardUpToThenDrawThatManyEffect.ANY_NUMBER;
        String discardPhrase = anyNumber
                ? "Discard any number of cards"
                : "Discard up to " + e.maxDiscard() + " cards";
        if (e.extraDraw() > 0) {
            return discardPhrase + " for " + cardName
                    + ". You will draw that many cards plus " + e.extraDraw() + ".";
        }
        return discardPhrase + " for " + cardName + ". You will draw that many cards.";
    }
}
