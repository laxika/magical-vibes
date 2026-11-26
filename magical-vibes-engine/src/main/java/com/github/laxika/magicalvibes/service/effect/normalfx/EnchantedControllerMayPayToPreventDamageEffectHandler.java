package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerMayPayToPreventDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.replacement.DamageJournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link EnchantedControllerMayPayToPreventDamageEffect} (Power Leak, Errant Minion): the enchanted
 * permanent's controller (the stack entry's {@code targetId}) may pay any amount of mana, then the source deals
 * {@code amount} damage to them, and X of that damage is prevented, X the mana paid. The prevention is a shield of X
 * against the source's damage to that player (CR 615.1), an option of the player's damage pool ordered with the
 * others (CR 616.1); what it doesn't use ends once that damage is dealt, as it prevents only that damage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnchantedControllerMayPayToPreventDamageEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final GameQueryService gameQueryService;
    private final DamageJournalService damageJournalService;

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

        // Resolved again after the damage waited for the player to order its preventions: it was paid for already.
        if (damageJournalService.isResolvedAgain(gameData)) {
            dealDamage(gameData, entry, playerId, e.amount());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        // Re-entry after the enchanted controller chose how much mana to pay.
        if (gameData.chosenXValue != null) {
            int paid = gameData.chosenXValue;
            gameData.chosenXValue = null;

            // Paid mana prevents damage only while damage is preventable (Skullcrack); otherwise the
            // mana is still spent but the full amount is dealt.
            boolean preventable = gameQueryService.isDamagePreventable(gameData);
            int prevented = preventable ? paid : 0;
            if (paid > 0) {
                // Cap was based on potential mana so the player could tap lands during the
                // prompt; re-check the actual pool before charging.
                ManaPool pool = gameData.playerManaPools.get(playerId);
                if (payableFromPool(pool) < paid) {
                    gameLogService.append(gameData, GameLog.text(
                            playerName + " can't pay {" + paid + "} for " + cardName
                                    + " (tap mana sources, then choose the amount again)."));
                    log.info("Game {} - {} cannot yet pay {} for {} — re-prompting",
                            gameData.id, playerName, paid, cardName);
                    beginPayPrompt(gameData, playerId, e.amount(), cardName);
                    return;
                }
                new ManaCost("{" + paid + "}").payResolutionCost(pool);
                gameLogService.append(gameData, GameLog.text(preventable
                        ? playerName + " pays {" + paid + "} to prevent " + paid + " damage from " + cardName + "."
                        : playerName + " pays {" + paid + "}, but the damage from " + cardName + " can't be prevented."));
                log.info("Game {} - {} pays {} to prevent damage from {}", gameData.id, playerName, paid, cardName);
            }

            if (prevented > 0) {
                gameData.addShield(cardName, entry.getSourcePermanentId(), entry.getControllerId(), playerId,
                        new TargetSourceDamagePreventionShield(playerId, sourceKey(entry), prevented),
                        Duration.UNTIL_END_OF_TURN);
            }
            dealDamage(gameData, entry, playerId, e.amount());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }

        // First call: prompt the enchanted controller for how much mana to pay (0..amount).
        // The cap includes untapped mana sources so an empty pool with untapped lands still
        // opens the prompt (CR 605.3a — mana abilities during the payment).
        if (maxPayable(gameData, playerId) <= 0) {
            dealDamage(gameData, entry, playerId, e.amount());
            gameOutcomeService.checkWinCondition(gameData);
            return;
        }
        beginPayPrompt(gameData, playerId, e.amount(), cardName);
    }

    private void beginPayPrompt(GameData gameData, UUID playerId, int amount, String cardName) {
        int maxX = maxPayable(gameData, playerId);
        String prompt = "Pay any amount of mana to prevent that much of " + cardName + "'s "
                + amount + " damage to you?";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(playerId, maxX, prompt, cardName, true));
    }

    /** The pool and what the player's untapped sources could add (CR 605.3a), as this resolution payment spends it. */
    private int maxPayable(GameData gameData, UUID playerId) {
        return payableFromPool(potentialManaService.buildVirtualManaPoolForSpecialAction(gameData, playerId));
    }

    /**
     * The most generic mana {@code pool} pays as a resolution-time cost: it isn't a spell, so mana that can't be spent
     * to cast spells pays it, and mana that can be spent only on spells doesn't ({@link ManaCost#canPayResolutionCost}).
     */
    private static int payableFromPool(ManaPool pool) {
        int low = 0;
        int high = Math.max(0, pool.getTotalAllMana());
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            if (new ManaCost("{" + candidate + "}").canPayResolutionCost(pool)) {
                low = candidate;
            } else {
                high = candidate - 1;
            }
        }
        return low;
    }

    /**
     * Deals the damage; once it is dealt (not waiting for the player's order), the payment's prevention has
     * prevented all it may, so what it didn't use ends.
     */
    private void dealDamage(GameData gameData, StackEntry entry, UUID playerId, int amount) {
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, amount);
        if (damageJournalService.isHoldingEvents(gameData)) {
            return;
        }
        UUID sourceKey = sourceKey(entry);
        for (FloatingContinuousEffect fe : gameData.shieldEffects(TargetSourceDamagePreventionShield.class)) {
            var shield = (TargetSourceDamagePreventionShield) fe.effect();
            if (shield.targetId().equals(playerId) && shield.sourceId().equals(sourceKey)
                    && entry.getCard().getName().equals(fe.sourceCardName())) {
                gameData.endFloatingEffect(fe);
            }
        }
    }

    /** The identity the player's damage pool keys a source's shields by: the source permanent, or the card. */
    private static UUID sourceKey(StackEntry entry) {
        return entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getCard().getId();
    }
}
