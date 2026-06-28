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
                return;
            }

            // Store the draw count for after discards complete
            gameData.pendingRummageDrawCount = chosenCount;
            gameData.discardCausedByOpponent = false;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, chosenCount);
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " has no cards to discard for " + cardName + ".");
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        int maxDiscard = Math.min(e.maxDiscard(), hand.size());
        String prompt = "Discard up to " + e.maxDiscard() + " cards for " + cardName
                + ". You will draw that many cards.";
        playerInputService.beginXValueChoice(gameData, controllerId, maxDiscard, prompt, cardName);
    
    }
}
