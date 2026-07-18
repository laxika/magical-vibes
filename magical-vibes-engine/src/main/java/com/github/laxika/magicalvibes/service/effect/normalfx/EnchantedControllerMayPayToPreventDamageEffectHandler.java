package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerMayPayToPreventDamageEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link EnchantedControllerMayPayToPreventDamageEffect} (Power Leak): the enchanted
 * permanent's controller (the stack entry's {@code targetId}) may pay any amount of mana, then the
 * source deals {@code amount} damage to them with that much prevented. The payment prompt is capped
 * at {@code amount} (prevention beyond the damage dealt is pointless); the remaining
 * {@code amount - paid} damage goes through the normal damage system.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnchantedControllerMayPayToPreventDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EnchantedControllerMayPayToPreventDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EnchantedControllerMayPayToPreventDamageEffect) effect;
        UUID playerId = entry.getTargetId();
        if (!gameData.playerIds.contains(playerId)) return;

        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(playerId);

        // Re-entry after the enchanted controller chose how much mana to pay.
        if (gameData.chosenXValue != null) {
            int paid = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (paid > 0) {
                ManaPool pool = gameData.playerManaPools.get(playerId);
                new ManaCost("{0}").pay(pool, paid);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        playerName + " pays {" + paid + "} to prevent " + paid + " damage from " + cardName + "."));
                log.info("Game {} - {} pays {} to prevent damage from {}", gameData.id, playerName, paid, cardName);
            }

            dealRemainingDamage(gameData, entry, playerId, e.amount() - paid);
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        // First call: prompt the enchanted controller for how much mana to pay (0..amount).
        ManaPool pool = gameData.playerManaPools.get(playerId);
        int available = pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
        int maxX = Math.min(e.amount(), available);
        if (maxX <= 0) {
            dealRemainingDamage(gameData, entry, playerId, e.amount());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        String prompt = "Pay any amount of mana to prevent that much of " + cardName + "'s "
                + e.amount() + " damage to you?";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(playerId, maxX, prompt, cardName));
    }

    private void dealRemainingDamage(GameData gameData, StackEntry entry, UUID playerId, int remaining) {
        if (remaining <= 0) return;
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, remaining, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
    }
}
