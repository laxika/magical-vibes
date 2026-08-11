package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves a controller's optional, chosen discard of any number of cards. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardAnyNumberEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardAnyNumberEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var discardAnyNumber = (DiscardAnyNumberEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();

        if (gameData.chosenXValue != null) {
            int chosenCount = gameData.chosenXValue;
            gameData.chosenXValue = null;
            entry.setEventValue(chosenCount);

            if (chosenCount == 0) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + " chooses to discard 0 cards for " + cardName + "."));
                return;
            }

            gameData.discardCausedByOpponent = false;
            if (discardAnyNumber.random()) {
                playerInteractionSupport.resolveRandomDiscardCards(gameData, controllerId, cardName, chosenCount);
            } else {
                playerInteractionSupport.resolveDiscardCards(gameData, controllerId, chosenCount);
            }
            return;
        }

        entry.setEventValue(0);
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + " has no cards to discard for " + cardName + "."));
            log.info("Game {} - {} has no cards to discard for {}", gameData.id, playerName, cardName);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, hand.size(),
                        "Discard any number of cards for " + cardName + ".", cardName));
    }
}
