package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaDealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link PayXManaDealXDamageToAnyTargetEffect}: prompts the controller for X (capped by
 * how much of {@code manaCost} they can pay), charges {@code manaCost}, then deals X damage to the
 * target chosen when the ability was put on the stack. Choosing X=0 declines.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaDealXDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaDealXDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaDealXDamageToAnyTargetEffect) effect;
        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Re-entry after the player chose X value
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " declines to pay for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            new ManaCost(e.manaCost()).pay(pool, chosenValue);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " pays " + e.manaCost().replace("{X}", "{" + chosenValue + "}") + " for " + cardName + "."));
            log.info("Game {} - {} pays X={} for {}", gameData.id, playerName, chosenValue, cardName);

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, chosenValue, entry);
            damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        // First call: prompt for X value (capped by how much of manaCost the pool can pay)
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int maxX = new ManaCost(e.manaCost()).calculateMaxX(pool);
        if (maxX <= 0) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " can't pay for " + cardName + "'s ability."));
            log.info("Game {} - {} can't pay {} for {}", gameData.id, playerName, e.manaCost(), cardName);
            return;
        }
        String prompt = "Pay " + e.manaCost() + " for " + cardName + "? It deals X damage to the target.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName));
    }
}
