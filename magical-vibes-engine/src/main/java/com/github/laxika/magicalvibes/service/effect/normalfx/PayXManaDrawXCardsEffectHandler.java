package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDrawXCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves Well of Lost Dreams' optional pay-X life-gain trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaDrawXCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaDrawXCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        int lifeGained = Math.max(0, entry.getEventValue());

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + " chooses not to pay for " + cardName + "."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (chosenValue > lifeGained || payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName, lifeGained);
                return;
            }

            new ManaCost("{0}").pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + chosenValue + "} for " + cardName
                            + " and draws " + chosenValue + " card"
                            + (chosenValue == 1 ? "." : "s.")));
            log.info("Game {} - {} pays {} mana and draws {} for {}", gameData.id, playerName,
                    chosenValue, chosenValue, cardName);
            playerInteractionSupport.applyDrawCards(gameData, controllerId, chosenValue);
            return;
        }

        beginXPrompt(gameData, controllerId, cardName, lifeGained);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName, int lifeGained) {
        int maxX = Math.min(lifeGained, maxPotentialX(gameData, controllerId));
        if (maxX <= 0) {
            return;
        }
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxX,
                        "You may pay {X} for " + cardName + " to draw X cards.",
                        cardName,
                        true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - pool.getTotal();
        return Math.max(0, payableFromPool(pool) + untappedSources);
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
    }
}
