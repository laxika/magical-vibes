package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Handles end-of-turn cleanup, mana pool draining, and hand-size calculations.
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate the cleanup-step
 * responsibilities (CR 514) into a focused service.  Key duties:
 * <ul>
 *   <li>Resetting "until end of turn" modifiers on permanents and global flags.</li>
 *   <li>Draining all mana pools between phases (unless prevented by e.g. Upwelling).</li>
 *   <li>Computing each player's effective maximum hand size, accounting for
 *       effects that reduce it ({@link ReduceOpponentMaxHandSizeEffect}) or
 *       remove it entirely ({@link NoMaximumHandSizeEffect}).</li>
 *   <li>Returning stolen creatures at end of turn via {@link AuraAttachmentService}.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TurnCleanupService {

    private final AuraAttachmentService auraAttachmentService;

    /**
     * Performs the full cleanup-step reset: clears all "until end of turn"
     * modifiers on every permanent and returns temporarily stolen creatures.
     *
     * @param gameData the current game state to modify
     */
    public void applyCleanupResets(GameData gameData) {
        resetEndOfTurnModifiers(gameData);
        auraAttachmentService.returnStolenCreatures(gameData, true);
    }

    /**
     * Resets all "until end of turn" modifiers on permanents (power/toughness
     * modifiers, granted keywords, damage-prevention and regeneration shields,
     * animation flags) and clears global damage-prevention state.
     *
     * @param gameData the current game state to modify
     */
    public void resetEndOfTurnModifiers(GameData gameData) {
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getPowerModifier() != 0 || p.getToughnessModifier() != 0 || !p.getGrantedKeywords().isEmpty()
                    || p.getDamagePreventionShield() != 0 || p.getRegenerationShield() != 0 || p.isCantBeBlocked()
                    || p.isAnimatedUntilEndOfTurn() || p.isCantRegenerateThisTurn()
                    || p.isExileInsteadOfDieThisTurn() || !p.getGrantedCardTypes().isEmpty()
                    || p.isMustAttackThisTurn()) {
                p.resetModifiers();
                p.setDamagePreventionShield(0);
                p.setRegenerationShield(0);
            }
        });

        gameData.playerDamagePreventionShields.clear();
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.allPermanentsEnterTappedThisTurn = false;
        gameData.preventDamageFromColors.clear();
        gameData.combatDamageRedirectTarget = null;
        gameData.playerColorDamagePreventionCount.clear();
        gameData.playerSourceDamagePreventionIds.clear();
        gameData.permanentsPreventedFromDealingDamage.clear();
        gameData.drawReplacementTargetToController.clear();
    }

    /**
     * Empties every player's mana pool, unless a permanent with
     * {@link PreventManaDrainEffect} (e.g. Upwelling) is on any battlefield.
     *
     * @param gameData the current game state to modify
     */
    public void drainManaPools(GameData gameData) {
        // Check if any permanent on the battlefield prevents mana drain (e.g. Upwelling)
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PreventManaDrainEffect.class::isInstance)) {
                    return;
                }
            }
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clear();
            }
        }
    }

    /**
     * Calculates the effective maximum hand size for the given player.
     * Starts at the default 7, then subtracts reductions from opponents'
     * {@link ReduceOpponentMaxHandSizeEffect} permanents.
     *
     * @param gameData the current game state
     * @param playerId the player whose hand-size limit to compute
     * @return the computed maximum hand size (may be negative before clamping)
     */
    public int getMaxHandSize(GameData gameData, UUID playerId) {
        int maxHandSize = 7;
        // Check all opponents' battlefields for ReduceOpponentMaxHandSizeEffect
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            if (otherPlayerId.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(otherPlayerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ReduceOpponentMaxHandSizeEffect reduce) {
                        maxHandSize -= reduce.reduction();
                    }
                }
            }
        }
        return maxHandSize;
    }

    /**
     * Checks whether the given player's hand size is unlimited, either via
     * the {@code playersWithNoMaximumHandSize} set on {@link GameData} or by
     * controlling a permanent with {@link NoMaximumHandSizeEffect} (e.g. Spellbook).
     *
     * @param gameData the current game state
     * @param playerId the player to check
     * @return {@code true} if the player has no maximum hand size
     */
    public boolean hasNoMaximumHandSize(GameData gameData, UUID playerId) {
        if (gameData.playersWithNoMaximumHandSize.contains(playerId)) {
            return true;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                return true;
            }
        }
        return false;
    }
}
