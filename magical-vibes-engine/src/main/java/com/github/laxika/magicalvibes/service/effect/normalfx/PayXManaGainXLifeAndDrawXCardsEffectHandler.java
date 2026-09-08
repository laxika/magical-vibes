package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeAndDrawXCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaGainXLifeAndDrawXCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final LifeSupport lifeSupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaGainXLifeAndDrawXCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PayXManaGainXLifeAndDrawXCardsEffect e = (PayXManaGainXLifeAndDrawXCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        ManaCost cost = new ManaCost(e.manaCost());

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            if (!cost.canPay(gameData.playerManaPools.get(controllerId), chosenValue)) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay " + formatCost(e.manaCost(), chosenValue) + " for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cost, cardName, e.manaCost());
                return;
            }

            cost.pay(gameData.playerManaPools.get(controllerId), chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays " + formatCost(e.manaCost(), chosenValue) + " for " + cardName
                            + ", gains " + chosenValue + " life, and draws " + chosenValue + " cards."));
            lifeSupport.applyGainLife(gameData, controllerId, chosenValue, cardName);
            playerInteractionSupport.applyDrawCards(gameData, controllerId, chosenValue);
            return;
        }

        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, controllerId));
        if (maxX <= 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no mana to pay for " + cardName + "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X ability", gameData.id, playerName, cardName);
            return;
        }
        beginXPrompt(gameData, controllerId, cost, cardName, e.manaCost());
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, ManaCost cost, String cardName, String manaCost) {
        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, controllerId));
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX,
                        "You may pay " + manaCost + " for " + cardName
                                + ". Gain X life and draw X cards.", cardName, true));
    }

    private static String formatCost(String manaCost, int chosenValue) {
        return manaCost.replace("{X}", "{" + chosenValue + "}");
    }
}
