package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeDrawXCardsEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PayXLifeDrawXCardsEffect}: prompts the controller for X (capped at their current
 * life total), pays that much life, then draws that many cards. Necrologia.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXLifeDrawXCardsEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXLifeDrawXCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Re-entry after the player chose X.
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " pays 0 life for " + cardName + " and draws no cards."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            lifeSupport.applyLifeLoss(gameData, controllerId, chosenValue, cardName);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " pays " + chosenValue + " life for " + cardName + " and draws " + chosenValue + " cards."));
            log.info("Game {} - {} pays {} life and draws {} for {}", gameData.id, playerName,
                    chosenValue, chosenValue, cardName);
            playerInteractionSupport.applyDrawCards(gameData, controllerId, chosenValue);
            return;
        }

        // First call: prompt for X. A player can't pay more life than they have.
        int maxX = gameData.getLife(controllerId);
        if (maxX <= 0) {
            log.info("Game {} - {} has no life to pay for {}", gameData.id, playerName, cardName);
            return;
        }
        String prompt = "Pay X life for " + cardName + "? Draw X cards.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName));
    }
}
