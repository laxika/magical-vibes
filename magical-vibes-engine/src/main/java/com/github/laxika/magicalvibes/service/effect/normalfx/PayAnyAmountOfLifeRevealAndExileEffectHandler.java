package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeRevealAndExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PayAnyAmountOfLifeRevealAndExileEffect} (Vizkopa Confessor): the controller picks
 * an amount of life between 0 and their current total through an {@code XValueChoice} and pays it,
 * then the target opponent reveals that many cards of their choice from their hand and the
 * controller exiles one of them. The reveal-and-choose stage reuses the Blackmail flow
 * ({@link PlayerInteractionSupport#beginRevealCardsChooseDiscard}) with an
 * {@link HandChoiceDestination#EXILE} destination. Paying 0 life reveals and exiles nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayAnyAmountOfLifeRevealAndExileEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayAnyAmountOfLifeRevealAndExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Re-entry after the controller chose how much life to pay.
        if (gameData.chosenXValue != null) {
            int paid = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (paid <= 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " pays no life for " + cardName + "."));
                log.info("Game {} - {} pays 0 life for {}", gameData.id, playerName, cardName);
                return;
            }

            lifeSupport.applyLifeLoss(gameData, controllerId, paid, cardName);
            gameLogService.append(gameData, GameLog.text(playerName + " pays " + paid + " life for " + cardName + "."));
            log.info("Game {} - {} pays {} life for {}", gameData.id, playerName, paid, cardName);
            playerInteractionSupport.beginRevealCardsChooseDiscard(gameData, entry, paid, 1,
                    HandChoiceDestination.EXILE);
            return;
        }

        // A player can't pay more life than they have.
        int maxLife = gameData.getLife(controllerId);
        if (maxLife <= 0) {
            log.info("Game {} - {} has no life to pay for {}", gameData.id, playerName, cardName);
            return;
        }
        String prompt = "Pay any amount of life for " + cardName + "? Target opponent reveals that many cards.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxLife, prompt, cardName));
    }
}
