package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves {@link PayXLifeEffect} and stores the chosen amount as the entry's event value. */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayXLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;
            entry.setEventValue(chosenValue);

            if (chosenValue > 0) {
                lifeSupport.applyLifeLoss(gameData, controllerId, chosenValue, cardName);
            }
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays " + chosenValue + " life for " + cardName + "."));
            log.info("Game {} - {} pays {} life for {}", gameData.id, playerName, chosenValue, cardName);
            return;
        }

        if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
            entry.setEventValue(0);
            return;
        }

        int maxX = Math.max(0, gameData.getLife(controllerId));
        if (maxX == 0) {
            entry.setEventValue(0);
            return;
        }
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX,
                        "Pay X life for " + cardName + ".", cardName));
    }
}
