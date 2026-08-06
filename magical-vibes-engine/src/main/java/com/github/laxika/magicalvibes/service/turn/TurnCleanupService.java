package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
 *       remove it entirely ({@link NoMaximumHandSizeEffect} /
 *       {@link PlayersHaveNoMaximumHandSizeEffect}).</li>
 *   <li>Reconciling control at end of turn via {@link CreatureControlService} — expired
 *       until-end-of-turn control effects fall back to the next most recent still-active
 *       control effect, or to the owner (CR 613.7).</li>
 * </ul>
 */
@Slf4j
@Service
public class TurnCleanupService {

    private final CreatureControlService creatureControlService;
    private final PermanentRemovalService permanentRemovalService;

    public TurnCleanupService(CreatureControlService creatureControlService,
                              @Lazy PermanentRemovalService permanentRemovalService) {
        this.creatureControlService = creatureControlService;
        this.permanentRemovalService = permanentRemovalService;
    }

    /**
     * Performs the full cleanup-step reset: sacrifices permanents whose controller must sacrifice
     * them at the beginning of this cleanup step, clears all "until end of turn" modifiers on every
     * permanent and recomputes control of temporarily stolen permanents.
     *
     * @param gameData the current game state to modify
     */
    public void applyCleanupResets(GameData gameData) {
        sacrificePermanentsFlaggedForCleanup(gameData);
        returnPermanentsFlaggedForCleanup(gameData);
        removeCountersScheduledForCleanup(gameData);
        resetEndOfTurnModifiers(gameData);
        tapPermanentsReturningToOwner(gameData);
        creatureControlService.reconcileControl(gameData);
    }

    /**
     * Sacrifices every permanent carrying the Mirage flash clause's "sacrifice it at the beginning
     * of the next cleanup step" rider (Ward of Lights cast at instant speed).
     */
    private void sacrificePermanentsFlaggedForCleanup(GameData gameData) {
        List<Permanent> doomed = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isSacrificeAtNextCleanup()) {
                doomed.add(p);
            }
        });
        if (doomed.isEmpty()) {
            return;
        }
        for (Permanent permanent : doomed) {
            permanent.setSacrificeAtNextCleanup(false);
            permanentRemovalService.removePermanentToGraveyard(gameData, permanent);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Returns every permanent carrying a "return this to its owner's hand at the beginning of the
     * next cleanup step" rider (Thawing Glaciers) to its owner's hand.
     */
    private void returnPermanentsFlaggedForCleanup(GameData gameData) {
        List<Permanent> returning = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isReturnToHandAtNextCleanup()) {
                returning.add(p);
            }
        });
        if (returning.isEmpty()) {
            return;
        }
        for (Permanent permanent : returning) {
            permanent.setReturnToHandAtNextCleanup(false);
            permanentRemovalService.removePermanentToHand(gameData, permanent);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    /**
     * Sheds counters scheduled by a "remove a counter from that creature at the beginning of the
     * next cleanup step" rider (Bounty of the Hunt). Removal is clamped to the counters the
     * creature still has, so a creature that already lost them keeps a non-negative count.
     */
    private void removeCountersScheduledForCleanup(GameData gameData) {
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getCountersToRemoveAtNextCleanup().isEmpty()) {
                return;
            }
            p.getCountersToRemoveAtNextCleanup().forEach((counterType, amount) ->
                    p.setCounterCount(counterType, Math.max(0, p.getCounterCount(counterType) - amount)));
            p.getCountersToRemoveAtNextCleanup().clear();
        });
    }

    /**
     * Taps permanents carrying a "tap it when you lose control" rider (Magus of the Unseen) as
     * their until-end-of-turn control effect expires this cleanup and they revert to their owner.
     */
    private void tapPermanentsReturningToOwner(GameData gameData) {
        if (gameData.permanentsToTapWhenControlLost.isEmpty()) {
            return;
        }
        for (UUID permanentId : gameData.permanentsToTapWhenControlLost) {
            Permanent permanent = findPermanent(gameData, permanentId);
            if (permanent != null) {
                permanent.tap();
            }
        }
        gameData.permanentsToTapWhenControlLost.clear();
    }

    /**
     * Resets all "until end of turn" modifiers on permanents (power/toughness
     * modifiers, granted keywords, damage-prevention and regeneration shields,
     * animation flags) and clears global damage-prevention state.
     *
     * @param gameData the current game state to modify
     */
    public void resetEndOfTurnModifiers(GameData gameData) {
        // CR 613 layer engine: "until end of turn" floating continuous effects wear off here,
        // before the legacy per-permanent modifier reset below. An expiring layer-1 copy effect
        // (Tilonalli's Skinshifter) reverts the permanent's card to the pre-copy card — the
        // official ruling pins this to the same moment damage is removed (the cleanup step).
        for (FloatingContinuousEffect expired : gameData.expireEndOfTurnFloatingEffects()) {
            if (expired.effect() instanceof BecomeCopyOfTargetCreatureUntilEndOfTurnEffect
                    && expired.affectedPermanentId() != null) {
                Permanent copy = findPermanent(gameData, expired.affectedPermanentId());
                if (copy != null) {
                    copy.revertEndOfTurnCopy();
                }
            }
        }

        gameData.forEachPermanent((playerId, p) -> {
            // CR 514.2 — remove all damage marked on permanents during cleanup step
            p.setMarkedDamage(0);
            p.setDamagedByDeathtouch(false);
            p.setTimesRegeneratedThisTurn(0);
            // Reset unconditionally: resetModifiers() only touches "until end of turn" state, so it is a
            // no-op on an unmodified permanent. Guarding it on a hand-maintained list of dirty flags let
            // state it clears (must-be-blocked, must-block, transient type overrides, ...) survive cleanup
            // whenever that was the permanent's only modification.
            p.resetModifiers();
            p.setDamagePreventionShield(0);
            p.setDamageToCounterPreventionShield(0);
            p.setRegenerationShield(0);
            p.setOpponentDrawRegenerationShield(0);
            p.setMinusOneCounterRegenerationShield(0);
        });

        gameData.playerDamagePreventionShields.clear();
        gameData.damageRedirectShields.clear();
        gameData.sourceDamageRedirectShields.clear();
        gameData.creatureDamageRedirectShields.clear();
        gameData.turnDamageRedirectToCreatureShields.clear();
        gameData.playerNextDamageRedirectShields.clear();
        gameData.targetSourceDamagePreventionShields.clear();
        gameData.damagePreventionLifeGainShields.clear();
        gameData.globalDamagePreventionShield = 0;
        gameData.preventAllCombatDamage = false;
        gameData.preventAllDamageToAllCreatures = false;
        gameData.preventAllDamageByCreatures = false;
        gameData.combatDamageExemptPredicate = null;
        gameData.allPermanentsEnterTappedThisTurn = false;
        gameData.additionalEnterCountersThisTurn.clear();
        gameData.preventDamageFromColors.clear();
        gameData.combatDamageRedirectTarget = null;
        gameData.playerColorDamagePreventionCount.clear();
        gameData.colorDamagePreventionUntilEndOfTurn.clear();
        gameData.playerSourceDamagePreventionIds.clear();
        gameData.playerSourceNextDamageShields.clear();
        gameData.sourceNextDamageToAnyTargetShields.clear();
        gameData.eyeForAnEyeShields.clear();
        gameData.reflectDamageToSourceControllerShields.clear();
        gameData.pendingEyeForAnEyeReflections.clear();
        gameData.pendingSourceDamageForReflection.clear();
        gameData.permanentsPreventedFromDealingDamage.clear();
        gameData.playersWithAllDamagePrevented.clear();
        gameData.playersWithDamageFromAttackersPrevented.clear();
        gameData.playersGatheringSpecimensThisTurn.clear();
        gameData.creaturesWithAllDamagePrevented.clear();
        gameData.allDamagePreventionPredicates.clear();
        gameData.creaturesWithCombatDamagePrevented.clear();
        gameData.creaturesPreventedFromDealingCombatDamage.clear();
        gameData.damageCantBePreventedThisTurn = false;
        gameData.playersCantGainLifeThisTurn = false;
        gameData.combatDamageToCreaturesDoublingsThisTurn = 0;
        gameData.controllerDamageDoublingsThisTurn.clear();
        gameData.opponentGraveyardLifeLossWatchers.clear();
        gameData.lifeGainOpponentLifeLossWatchers.clear();
        gameData.drawReplacementTargetToController.clear();
        gameData.pendingNextDrawLookAtTop.clear();
        gameData.pendingNextDrawFromExiledPile.clear();
        gameData.colorSourceDamageBonusThisTurn.clear();
        gameData.playerSpellsCantBeCounteredByColorsThisTurn.clear();
        gameData.playerCreaturesCantBeTargetedByColorsThisTurn.clear();
        gameData.playerProtectionFromColorsUntilEndOfTurn.clear();
        gameData.spellColorOverridesUntilEndOfTurn.clear();
        gameData.playersSilencedThisTurn.clear();
        gameData.extraManaOnLandSubtypeTapThisTurn.clear();
        gameData.landSubtypeFixedManaColorThisTurn.clear();
        gameData.allLandsFixedManaColorThisTurn = null;
        gameData.playersCantPlayLandsThisTurn.clear();
        gameData.playersCantCastCreatureSpellsThisTurn.clear();
        gameData.playersCantCastNoncreatureSpellsThisTurn.clear();
        gameData.playersCantActivateAbilitiesThisTurn.clear();
        gameData.senControllerPlayerId = null;
        gameData.senControlledPlayerId = null;
        gameData.cardsGrantedFlashbackUntilEndOfTurn.clear();
        gameData.playersWithFlashUntilEndOfTurn.clear();
        gameData.nextSpellFlashGrantsThisTurn.clear();
        gameData.nextCreatureSpellEmpowermentsThisTurn.clear();
        gameData.spellAdditionalEnterCounters.clear();
        gameData.spellsGrantedHasteOnEntry.clear();
        gameData.mayTapLandsForSpellsUntilEndOfTurn.clear();
        gameData.mayPayLifeForColorlessManaUntilEndOfTurn.clear();
        gameData.graveyardCreatureCastPermissionsUntilEndOfTurn.clear();
        for (var cardId : gameData.graveyardPlayPermissionsExpireEndOfTurn) {
            gameData.graveyardPlayPermissions.remove(cardId);
        }
        gameData.graveyardPlayPermissionsExpireEndOfTurn.clear();
        gameData.playersWithSpellCopyUntilEndOfTurn.clear();
        gameData.pendingNextInstantSorceryCopyThisTurnCount.clear();
        gameData.conspiredSpellIds.clear();

        // Defensive reset of graveyard-leave batching state (always balanced via try/finally,
        // but guard against any leaked batch depth across turns).
        gameData.graveyardLeaveNotificationDepth = 0;
        gameData.graveyardLeaveNotificationPendingOwners.clear();
        gameData.playersWhoseCardsLeftGraveyardThisTurn.clear();

        // Remove temporary impulse-draw exile permissions (e.g. Vance's Blasting Cannons)
        for (var cardId : gameData.exilePlayPermissionsExpireEndOfTurn) {
            gameData.exilePlayPermissions.remove(cardId);
        }
        gameData.exilePlayPermissionsExpireEndOfTurn.clear();

        // Per-card "this turn" exile-cast riders (e.g. Nita, Forum Conciliator) end with the turn.
        gameData.exilePlayAnyManaType.clear();
        gameData.exilePlayWithoutPayingManaCost.clear();
        gameData.exileInsteadOfGraveyard.clear();

        int currentTurn = gameData.turnNumber;
        gameData.exilePlayPermissionsExpireAtTurnEnd.entrySet().removeIf(entry -> {
            if (entry.getValue() <= currentTurn) {
                gameData.exilePlayPermissions.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Clear persistent mana tracking so the next drain empties pools fully
        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clearPersistentMana();
            }
        }
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent p : bf) {
                if (p.getId().equals(permanentId)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Empties every player's mana pool, unless a permanent with
     * {@link PreventManaDrainEffect} (e.g. Upwelling) is on any battlefield.
     * Mana marked as persistent (e.g. from Grand Warlord Radha) survives
     * the drain; only non-persistent mana is removed.
     *
     * @param gameData the current game state to modify
     */
    public void drainManaPools(GameData gameData) {
        // Check if any permanent on the battlefield prevents mana drain globally (e.g. Upwelling)
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
                manaPool.drainNonPersistent();
            }
        }

        // Clear pending one-shot spell copy triggers (Primal Wellspring) since their mana drained
        gameData.pendingNextInstantSorceryCopyCount.clear();
    }

    /**
     * Calculates the effective maximum hand size for the given player.
     * Starts at the default 7, then applies each opponent-controlled
     * {@link OpponentMaxHandSizeEffect} (e.g. {@link ReduceOpponentMaxHandSizeEffect} reducing by
     * N, {@link SetOpponentMaximumHandSizeEffect} setting to a specific value) to the running
     * value in battlefield/timestamp order (CR 402.2).
     *
     * @param gameData the current game state
     * @param playerId the player whose hand-size limit to compute
     * @return the computed maximum hand size (may be negative before clamping)
     */
    public int getMaxHandSize(GameData gameData, UUID playerId) {
        int maxHandSize = 7;
        // Fold every opponent-controlled hand-size effect over the running value in timestamp order.
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            if (otherPlayerId.equals(playerId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(otherPlayerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof OpponentMaxHandSizeEffect handSizeEffect) {
                        maxHandSize = handSizeEffect.applyToMaximumHandSize(maxHandSize);
                    }
                }
            }
        }
        return maxHandSize;
    }

    /**
     * Checks whether the given player's hand size is unlimited, either via
     * the {@code playersWithNoMaximumHandSize} and
     * {@code playersWithNoMaximumHandSizeUntilNextTurn} sets on {@link GameData}, by
     * controlling a permanent with {@link NoMaximumHandSizeEffect} (e.g. Spellbook),
     * or by any player controlling a permanent with
     * {@link PlayersHaveNoMaximumHandSizeEffect} (e.g. Anvil of Bogardan).
     *
     * @param gameData the current game state
     * @param playerId the player to check
     * @return {@code true} if the player has no maximum hand size
     */
    public boolean hasNoMaximumHandSize(GameData gameData, UUID playerId) {
        if (gameData.playersWithNoMaximumHandSize.contains(playerId)
                || gameData.playersWithNoMaximumHandSizeUntilNextTurn.contains(playerId)) {
            return true;
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf != null) {
            for (Permanent perm : bf) {
                if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent perm : battlefield) {
                if (perm.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PlayersHaveNoMaximumHandSizeEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }
}
