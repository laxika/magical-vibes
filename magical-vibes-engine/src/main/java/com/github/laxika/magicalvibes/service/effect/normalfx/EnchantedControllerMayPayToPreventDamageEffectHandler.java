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
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
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
    private final PotentialManaService potentialManaService;

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
                // Cap was based on potential mana so the player could tap lands during the
                // prompt; re-check the actual pool before charging.
                ManaPool pool = gameData.playerManaPools.get(playerId);
                if (payableFromPool(pool) < paid) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                            playerName + " can't pay {" + paid + "} for " + cardName
                                    + " (tap mana sources, then choose the amount again)."));
                    log.info("Game {} - {} cannot yet pay {} for {} — re-prompting",
                            gameData.id, playerName, paid, cardName);
                    beginPayPrompt(gameData, playerId, e.amount(), cardName);
                    return;
                }
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
        // The cap includes untapped mana sources so an empty pool with untapped lands still
        // opens the prompt (CR 605.3a — mana abilities during the payment).
        if (maxPayable(gameData, playerId, e.amount()) <= 0) {
            dealRemainingDamage(gameData, entry, playerId, e.amount());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }
        beginPayPrompt(gameData, playerId, e.amount(), cardName);
    }

    private void beginPayPrompt(GameData gameData, UUID playerId, int amount, String cardName) {
        int maxX = maxPayable(gameData, playerId, amount);
        String prompt = "Pay any amount of mana to prevent that much of " + cardName + "'s "
                + amount + " damage to you?";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(playerId, maxX, prompt, cardName, true));
    }

    private int maxPayable(GameData gameData, UUID playerId, int amount) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, playerId).getTotal()
                - gameData.playerManaPools.get(playerId).getTotal();
        int available = payableFromPool(gameData.playerManaPools.get(playerId)) + untappedSources;
        return Math.min(amount, available);
    }

    /** Generic-payable mana in the pool right now — mirrors what {@code pay} can drain. */
    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
    }

    private void dealRemainingDamage(GameData gameData, StackEntry entry, UUID playerId, int remaining) {
        if (remaining <= 0) return;
        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, remaining, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
    }
}
