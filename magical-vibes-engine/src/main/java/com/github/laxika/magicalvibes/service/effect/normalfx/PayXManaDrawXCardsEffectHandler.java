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
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves optional generic pay-X draw triggers. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaDrawXCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaDrawXCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PayXManaDrawXCardsEffect e = (PayXManaDrawXCardsEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        int maximumX = e.maximumX() != null
                ? Math.max(0, amountEvaluationService.evaluate(gameData, e.maximumX(),
                        AmountContext.forStackEntry(entry, null)))
                : e.capAtEventValue() ? Math.max(0, entry.getEventValue()) : Integer.MAX_VALUE;

        ManaCost cost = new ManaCost("{X}");

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
            if (chosenValue > maximumX || !cost.canPay(pool, chosenValue)) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName, maximumX);
                return;
            }

            cost.pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + chosenValue + "} for " + cardName
                            + " and draws " + chosenValue + " card"
                            + (chosenValue == 1 ? "." : "s.")));
            log.info("Game {} - {} pays {} mana and draws {} for {}", gameData.id, playerName,
                    chosenValue, chosenValue, cardName);
            playerInteractionSupport.applyDrawCards(gameData, controllerId, chosenValue);
            return;
        }

        beginXPrompt(gameData, controllerId, cardName, maximumX);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName, int maximumX) {
        int maxX = maxPotentialX(gameData, controllerId, maximumX);
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

    private int maxPotentialX(GameData gameData, UUID controllerId, int lifeGained) {
        ManaPool pool = potentialManaService.buildVirtualManaPool(gameData, controllerId);
        ManaCost cost = new ManaCost("{X}");
        int low = 0;
        int high = lifeGained;
        while (low < high) {
            int mid = low + (int) (((long) high - low + 1) / 2);
            if (cost.canPay(pool, mid)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }
}
