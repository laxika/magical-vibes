package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.effect.GrantNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.EndOfTurnStateField;
import com.github.laxika.magicalvibes.model.EndOfTurnStateShare;
import com.github.laxika.magicalvibes.model.action.DelayedAction;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedFromAllCreaturesDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DamagePersistenceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatureDamagePersistenceEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersHaveNoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventManaDrainEffect;
import com.github.laxika.magicalvibes.model.effect.ReplaceManaDrainWithColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentMaxHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.RememberTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SetControllerMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SetOpponentMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ThreadLocal<Boolean> checkingGrantedHandSizeEffects = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<Boolean> computingMaxHandSize = ThreadLocal.withInitial(() -> false);

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService triggeredAbilityStackService;
    @Autowired
    @Lazy
    private GameQueryService gameQueryService;

    public TurnCleanupService(CreatureControlService creatureControlService,
                              @Lazy PermanentRemovalService permanentRemovalService) {
        this.creatureControlService = creatureControlService;
        this.permanentRemovalService = permanentRemovalService;
    }

    /**
     * Performs the full cleanup-step reset: puts the cleanup-step triggers that flagged permanents carry (such as
     * sacrificing a permanent) on the stack, clears all "until end of turn" modifiers on every
     * permanent and recomputes control of temporarily stolen permanents.
     *
     * @param gameData the current game state to modify
     */
    public void applyCleanupResets(GameData gameData) {
        sacrificePermanentsFlaggedForCleanup(gameData);
        returnPermanentsFlaggedForCleanup(gameData);
        removeCountersScheduledForCleanup(gameData);
        gameData.restoreBombardmentCards();
        resetEndOfTurnModifiers(gameData);
        creatureControlService.reconcileControl(gameData);
        gameData.retainControlLossUnattachTriggers();
    }

    /**
     * Puts a cleanup trigger on the stack for every permanent still carrying the Mirage flash clause's "sacrifice it
     * at the beginning of the next cleanup step" rider (Ward of Lights cast at instant speed). A resolved spell turns
     * the rider into its own delayed triggered ability as it enters, so this only catches a flag set some other
     * way. The sacrifice is a resolution step of that trigger, so it can ask its CR 616.1 questions before the
     * permanent leaves; the flag is cleared once the trigger owns it.
     */
    private void sacrificePermanentsFlaggedForCleanup(GameData gameData) {
        List<Permanent> doomed = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isSacrificeAtNextCleanup()) {
                doomed.add(p);
            }
        });
        for (Permanent permanent : doomed) {
            permanent.setSacrificeAtNextCleanup(false);
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            var entry = TriggeredAbilityStackService.madeAbility(
                    permanent.getCard(), controllerId,
                    permanent.getCard().getName() + "'s cleanup trigger",
                    List.of(new SacrificePermanentsEffect(1,
                            new PermanentIsSpecificPermanentPredicate(permanent.getId()),
                            SacrificeRecipient.EACH_PLAYER)));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
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
            p.getCountersToRemoveAtNextCleanup().forEach((counterType, amount) -> {
                int current = p.getCounterCount(counterType);
                int removed = Math.min(current, amount);
                p.setCounterCount(counterType, Math.max(0, current - amount));
                if (counterType == com.github.laxika.magicalvibes.model.CounterType.OIL) {
                    gameData.recordOilCounterRemoved(p, removed);
                }
            });
            p.getCountersToRemoveAtNextCleanup().clear();
        });
    }

    /**
     * Resets all "until end of turn" modifiers on permanents (power/toughness
     * modifiers, granted keywords, damage-prevention and regeneration shields,
     * animation flags) and clears global damage-prevention state.
     *
     * @param gameData the current game state to modify
     */
    public void resetEndOfTurnModifiers(GameData gameData) {
        // The "until end of turn" floating continuous effects end here (CR 514.2), before the per-permanent
        // modifier reset below; the duration clock reverts an expiring layer-1 copy (Tilonalli's Skinshifter).
        // Runed Terror ruling (CR 514.2): while players take their phases sequentially, an "until end
        // of turn" effect ends only at the cleanup step of the player who was active when it began.
        boolean phaseSequence = gameData.runedTerrorPhaseSequenceActive && gameData.activePlayerId != null;
        Map<UUID, EndOfTurnStateShare> retainedState = phaseSequence
                ? releaseActivePlayerEndOfTurnState(gameData)
                : Map.of();
        if (!phaseSequence) {
            gameData.phaseSequenceEndOfTurnShares.clear();
            gameData.phaseSequenceAttributedEndOfTurnState.clear();
        }
        gameData.rulesEvents.occur(new TurnEvent.Cleanup(gameData.activePlayerId));

        List<UUID> controllersWithOpponentDamagePersistence = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.hasLostPrintedAbilities(gameData, p)
                    && p.getCard().getStaticEffects().stream()
                    .anyMatch(OpponentCreatureDamagePersistenceEffect.class::isInstance)) {
                controllersWithOpponentDamagePersistence.add(playerId);
            }
        });

        boolean allCreaturesKeepDamage;
        synchronized (gameData.floatingEffects) {
            allCreaturesKeepDamage = gameData.floatingEffects.stream()
                    .anyMatch(fe -> fe.effect() instanceof GrantStaticEffectToSourceEffect grant
                            && grant.staticEffect() instanceof DamageNotRemovedFromAllCreaturesDuringCleanupEffect
                            && gameQueryService.findPermanentById(gameData, fe.sourcePermanentId()) != null);
        }

        gameData.forEachPermanent((playerId, p) -> {
            // CR 514.2 — remove all damage marked on permanents during cleanup step
            List<CardEffect> activeStaticEffects =
                    new ArrayList<>(gameQueryService.getActiveStaticEffects(gameData, p, playerId));
            activeStaticEffects.addAll(gameQueryService.getGrantedEffects(gameData, p));
            activeStaticEffects.addAll(p.getPersistentStaticEffects());
            boolean damagePersists = !p.isLosesAllAbilitiesUntilEndOfTurn()
                    && activeStaticEffects.stream().anyMatch(DamagePersistenceEffect.class::isInstance);
            if (!damagePersists && allCreaturesKeepDamage && isCreatureForCleanup(gameData, p)) {
                damagePersists = true;
            }
            if (!damagePersists && !controllersWithOpponentDamagePersistence.isEmpty()
                    && isCreatureForCleanup(gameData, p)) {
                damagePersists = controllersWithOpponentDamagePersistence.stream()
                        .anyMatch(sourceControllerId -> !sourceControllerId.equals(playerId));
            }
            if (!damagePersists) {
                p.setMarkedDamage(0);
                p.setDamagedByDeathtouch(false);
            }
            p.setTimesRegeneratedThisTurn(0);
            // Reset unconditionally: resetModifiers() only touches "until end of turn" state, so it is a
            // no-op on an unmodified permanent. Guarding it on a hand-maintained list of dirty flags let
            // state it clears (must-be-blocked, must-block, transient type overrides, ...) survive cleanup
            // whenever that was the permanent's only modification.
            p.resetModifiers();
            p.setDamagePreventionShield(0);
            p.setDamageToCounterPreventionShield(0);
            p.setDamageToPlusOnePlusOneCounterPreventionShield(0);
            p.setAllDamageToPlusOnePlusOneCounterPreventionShield(false);
            p.setDamageReplacedByPlusOnePlusOneCounters(false);
            p.setDamageDestructionShield(0);
            p.setLandDestructionShield(0);
            p.setRegenerationShield(0);
            p.setOpponentDrawRegenerationShield(0);
            p.getOpponentDrawRegenerationShieldRecipients().clear();
            p.getOpponentDrawRegenerationShieldControllers().clear();
            p.setMinusOneCounterRegenerationShield(0);
            p.setPlusOnePlusOneCounterRegenerationShield(0);
            p.getPlusOnePlusOneCounterRegenerationShieldControllers().clear();
            p.getGainControlRegenerationShields().clear();
            p.clearTappedPermanentsForAbilityThisTurn();
            EndOfTurnStateShare retained = retainedState.get(p.getId());
            if (retained != null) {
                p.addEndOfTurnState(retained);
            }
        });

        gameData.combatDamageExemptPredicate = null;
        gameData.combatDamageExemptControllerId = null;
        gameData.activeAdditionalCountersForEnchantmentCreatureEntryBatch.clear();
        gameData.sourceRedirectShieldsUsedInOpenEvent.clear();
        gameData.playerSourceShieldsUsedInOpenEvent.clear();
        gameData.pendingEyeForAnEyeReflections.clear();
        gameData.pendingSourceDamageForReflection.clear();
        gameData.playersWhoPlayedCardFromExileThisTurn.clear();
        gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.clear();
        gameData.opponentGraveyardLifeLossWatchers.clear();
        gameData.lifeGainOpponentLifeLossWatchers.clear();
        gameData.playersWhoseSpeedIncreasedThisTurn.clear();
        gameData.temporaryGlobalTriggeredAbilities.removeIf(watcher ->
                watcher.endsThisTurn()
                        || watcher.untilTriggered() && watcher.watchedPermanentId() != null
                        && !watcher.waitsForStep()
                        && gameQueryService.findPermanentById(gameData, watcher.watchedPermanentId()) == null
                        || (watcher.untilEndOfNextTurn()
                        && gameData.activePlayerId.equals(watcher.expirationPlayerId() != null
                        ? watcher.expirationPlayerId() : watcher.controllerId())
                        && gameData.turnNumber != watcher.registrationTurnNumber()));
        gameData.allyCreatureEntersTriggerWatchers.clear();
        gameData.chainsDrawReplacementsApplied.clear();
        gameData.drawStepFirstDrawTaken.clear();
        gameData.firstNonDrawStepDrawReplacementsUsedThisTurn.clear();
        gameData.activeMysticReflectionsForEntryBatch.clear();
        gameData.drawStepFirstDrawTaken.clear();
        gameData.firstNonDrawStepDrawReplacementsUsedThisTurn.clear();
        gameData.spellAdditionalEnterCounters.clear();
        gameData.spellAdditionalEnterCounterAmounts.clear();
        gameData.spellEntryCounters.clear();
        gameData.spellGrantedSubtypesOnEntry.clear();
        gameData.spellsGrantedHasteOnEntry.clear();
        gameData.spellsGrantedPermanentHasteOnEntry.clear();
        gameData.spellsGrantedRiotOnEntry.clear();
        EndOfTurnStateShare retainedGameState = retainedState.getOrDefault(
                GameData.GAME_END_OF_TURN_STATE_ID, EndOfTurnStateShare.EMPTY);
        gameData.outsideGamePlayPermissions.clear();
        gameData.outsideGameAdditionalModalModePermissions.clear();
        gameData.spellsPaidUsingPendingAnyManaTypeThisTurn.clear();
        gameData.conspiredSpellCopyCounts.clear();
        gameData.casualtySpellCopyCounts.clear();

        // Defensive reset of graveyard-leave batching state (always balanced via try/finally,
        // but guard against any leaked batch depth across turns).
        gameData.graveyardLeaveNotificationDepth = 0;
        gameData.graveyardExileChoiceLeaveBatchOpen = false;
        gameData.cardsExiledThisTurn = 0;
        gameData.playersWhoseCardsLeftGraveyardThisTurn.clear();
        gameData.playersWhoseCreatureCardsLeftGraveyardThisTurn.clear();
        gameData.cardsLeftGraveyardCountThisTurn.clear();
        gameData.creatureExileCountThisTurn.clear();

        gameData.playersWhoForetoldThisTurn.clear();
        gameData.exileInsteadOfGraveyard.clear();

        int currentTurn = gameData.turnNumber;
        gameData.addGameEndOfTurnState(retainedGameState);

        // Clear persistent mana tracking so the next drain empties pools fully
        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                manaPool.clearPersistentMana();
            }
        }
    }

    /**
     * Credits the active player with every permanent's and player's "until end of turn" / "this turn"
     * state that no player's share accounts for yet, i.e. what was created since the last attribution.
     * Called under Runed Terror's sequential phases whenever the active player is about to change and
     * at each cleanup step, so each player's share can end at that player's own cleanup step (Runed
     * Terror ruling, CR 514.2). A flag or set element that was already present when another player's
     * effect set it again stays credited to the first player only.
     *
     * @param gameData the current game state to modify
     */
    public void attributeEndOfTurnStateToActivePlayer(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId == null) {
            return;
        }
        captureEndOfTurnStates(gameData).forEach((objectId, current) -> {
            EndOfTurnStateShare attributed =
                    gameData.phaseSequenceAttributedEndOfTurnState.getOrDefault(objectId, EndOfTurnStateShare.EMPTY);
            EndOfTurnStateShare fresh = current.since(attributed);
            if (fresh.isEmpty()) {
                return;
            }
            gameData.phaseSequenceAttributedEndOfTurnState.put(objectId, attributed.plus(fresh));
            Map<UUID, EndOfTurnStateShare> shares =
                    gameData.phaseSequenceEndOfTurnShares.computeIfAbsent(objectId, id -> new LinkedHashMap<>());
            EndOfTurnStateShare previous = shares.remove(activePlayerId);
            shares.put(activePlayerId, previous == null ? fresh : previous.plus(fresh));
        });
    }

    /**
     * Attributes the outstanding state to the active player, drops that player's shares and returns,
     * per permanent or player id, the combined shares of the other players (limited to what is still
     * present), which outlive this cleanup step.
     */
    private Map<UUID, EndOfTurnStateShare> releaseActivePlayerEndOfTurnState(GameData gameData) {
        attributeEndOfTurnStateToActivePlayer(gameData);
        Map<UUID, EndOfTurnStateShare> current = captureEndOfTurnStates(gameData);
        Map<UUID, EndOfTurnStateShare> retained = new HashMap<>();
        gameData.phaseSequenceEndOfTurnShares.entrySet().removeIf(entry -> {
            UUID objectId = entry.getKey();
            Map<UUID, EndOfTurnStateShare> shares = entry.getValue();
            shares.remove(gameData.activePlayerId);
            EndOfTurnStateShare now = current.get(objectId);
            if (now == null || shares.isEmpty()) {
                gameData.phaseSequenceAttributedEndOfTurnState.remove(objectId);
                return true;
            }
            EndOfTurnStateShare kept = EndOfTurnStateShare.sum(shares.values()).cappedBy(now);
            retained.put(objectId, kept);
            gameData.phaseSequenceAttributedEndOfTurnState.put(objectId, kept);
            return false;
        });
        gameData.phaseSequenceAttributedEndOfTurnState.keySet()
                .retainAll(gameData.phaseSequenceEndOfTurnShares.keySet());
        return retained;
    }

    /** The current "until end of turn" state of every battlefield permanent, by id, plus
     *  the game-wide state under {@link GameData#GAME_END_OF_TURN_STATE_ID}. */
    private Map<UUID, EndOfTurnStateShare> captureEndOfTurnStates(GameData gameData) {
        Map<UUID, EndOfTurnStateShare> states = new HashMap<>();
        gameData.forEachPermanent((ownerId, permanent) ->
                states.put(permanent.getId(), permanent.captureEndOfTurnState()));
        states.put(GameData.GAME_END_OF_TURN_STATE_ID, gameData.captureGameEndOfTurnState());
        return states;
    }

    private boolean isCreatureForCleanup(GameData gameData, Permanent permanent) {
        return gameQueryService == null
                ? permanent.getCard().hasType(CardType.CREATURE)
                : gameQueryService.isCreature(gameData, permanent);
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
     * Drains each player's mana pool, applying global and controller-scoped prevention effects.
     * A controller's mana is converted to the replacement color instead when that controller has a
     * {@link ReplaceManaDrainWithColorlessEffect}. Mana marked as persistent survives the drain;
     * only non-persistent mana is removed.
     *
     * @param gameData the current game state to modify
     */
    public void drainManaPools(GameData gameData) {
        if (gameQueryService.activeStaticEffects(gameData, PreventManaDrainEffect.class).stream()
                .anyMatch(active -> active.effect().color() == null)) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            ManaPool manaPool = gameData.playerManaPools.get(playerId);
            if (manaPool != null) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                ManaColor replacementColor = battlefield == null ? null : battlefield.stream()
                        .filter(permanent -> !gameQueryService.hasLostAllAbilities(gameData, permanent))
                        .flatMap(permanent -> permanent.getCard().getStaticEffects().stream())
                        .filter(ReplaceManaDrainWithColorlessEffect.class::isInstance)
                        .map(ReplaceManaDrainWithColorlessEffect.class::cast)
                        .map(ReplaceManaDrainWithColorlessEffect::replacementColor)
                        .findFirst()
                        .orElse(null);
                Set<ManaColor> protectedColors = protectedManaColors(gameData, playerId);
                if (replacementColor != null) {
                    manaPool.convertNonPersistentManaTo(replacementColor, protectedColors);
                    protectedColors.add(replacementColor);
                }
                manaPool.drainNonPersistent(protectedColors);
            }
        }
    }

    private Set<ManaColor> protectedManaColors(GameData gameData, UUID playerId) {
        EnumSet<ManaColor> protectedColors = EnumSet.noneOf(ManaColor.class);
        for (GameQueryService.ActiveStaticEffect<PreventManaDrainEffect> active
                : gameQueryService.activeStaticEffects(gameData, playerId, PreventManaDrainEffect.class)) {
            if (active.effect().color() != null) {
                protectedColors.add(active.effect().color());
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (!playerId.equals(floating.affectedPlayerId())) {
                    continue;
                }
                if (floating.effect() instanceof PreventManaDrainEffect prevent) {
                    if (prevent.color() == null) {
                        protectedColors.addAll(Set.of(ManaColor.values()));
                    } else {
                        protectedColors.add(prevent.color());
                    }
                }
            }
        }
        return protectedColors;
    }

    /**
     * Calculates the effective maximum hand size for the given player.
     * Starts at the default 7, then applies each opponent-controlled
     * {@link OpponentMaxHandSizeEffect} (e.g. {@link ReduceOpponentMaxHandSizeEffect} reducing by
     * N, {@link SetOpponentMaximumHandSizeEffect} setting to a specific value) to the running
     * value in battlefield/timestamp order (CR 402.2), plus every controller-controlled
     * {@link ControllerMaxHandSizeEffect} (e.g. {@link SetControllerMaximumHandSizeEffect} —
     * Recycle's "your maximum hand size is two").
     *
     * @param gameData the current game state
     * @param playerId the player whose hand-size limit to compute
     * @return the computed maximum hand size (may be negative before clamping)
     */
    public int getMaxHandSize(GameData gameData, UUID playerId) {
        boolean nested = computingMaxHandSize.get();
        if (nested) {
            return computeMaxHandSize(gameData, playerId, true);
        }
        computingMaxHandSize.set(true);
        try {
            return computeMaxHandSize(gameData, playerId, false);
        } finally {
            computingMaxHandSize.remove();
        }
    }

    /**
     * Folds the hand-size effects into the maximum hand size. A nested query, reached while a condition
     * of a static ability is being evaluated for this same computation, sees only the unconditional
     * printed static effects, so a condition that asks about maximum hand size cannot recurse forever.
     */
    private int computeMaxHandSize(GameData gameData, UUID playerId, boolean nested) {
        int maxHandSize = 7;
        List<HandSizeModifier> modifiers = new ArrayList<>();
        int order = 0;
        // Fold every hand-size effect that applies to this player over the running value in
        // timestamp order — opponent-controlled ones from the other battlefields, and the
        // player's own "your maximum hand size is N" effects from theirs.
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            boolean own = otherPlayerId.equals(playerId);
            List<Permanent> bf = gameData.playerBattlefields.get(otherPlayerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                boolean hasChosenPlayer = perm.getCard().getOwnEntryEffects().stream()
                        .anyMatch(RememberTargetPlayerEffect.class::isInstance);
                boolean choosesOpponentAsEnters = perm.getCard().getStaticEffects().stream()
                        .anyMatch(com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect.class::isInstance);
                List<CardEffect> staticEffects = nested
                        ? unconditionalPrintedStaticEffects(gameData, perm)
                        : gameQueryService.getActiveStaticEffects(gameData, perm, otherPlayerId);
                for (CardEffect effect : staticEffects) {
                    if (effect instanceof OpponentMaxHandSizeEffect handSizeEffect) {
                        UUID affectedPlayerId = null;
                        if (hasChosenPlayer) {
                            affectedPlayerId = perm.getRememberedTargetPlayerId();
                            if (affectedPlayerId == null) {
                                UUID originalControllerId = gameData.stolenCreatures
                                        .getOrDefault(perm.getId(), otherPlayerId);
                                affectedPlayerId = gameData.orderedPlayerIds.stream()
                                        .filter(id -> !id.equals(originalControllerId))
                                        .findFirst()
                                        .orElse(null);
                            }
                        }
                        boolean affectsPlayer = choosesOpponentAsEnters && !perm.getChosenPlayerIds().isEmpty()
                                ? perm.getChosenPlayerIds().contains(playerId)
                                : hasChosenPlayer ? playerId.equals(affectedPlayerId) : !own;
                        if (affectsPlayer) {
                            modifiers.add(new HandSizeModifier(effect, perm, perm.getTimestamp(), order++, otherPlayerId));
                        }
                    } else if (own && effect instanceof ControllerMaxHandSizeEffect handSizeEffect) {
                        modifiers.add(new HandSizeModifier(effect, perm, perm.getTimestamp(), order++, otherPlayerId));
                    }
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floatingEffect : gameData.floatingEffects) {
                if (playerId.equals(floatingEffect.affectedPlayerId())
                        && (floatingEffect.effect() instanceof ControllerMaxHandSizeEffect
                        || floatingEffect.effect() instanceof OpponentMaxHandSizeEffect)) {
                    modifiers.add(new HandSizeModifier(floatingEffect.effect(), null,
                            floatingEffect.timestamp(), order++, floatingEffect.controllerId()));
                }
            }
        }
        modifiers.sort(Comparator.comparingLong(HandSizeModifier::timestamp)
                .thenComparingInt(HandSizeModifier::order));
        for (HandSizeModifier modifier : modifiers) {
            if (modifier.effect() instanceof ControllerMaxHandSizeEffect handSizeEffect) {
                maxHandSize = modifier.source() == null
                        ? handSizeEffect.applyToMaximumHandSize(maxHandSize)
                        : handSizeEffect.applyToMaximumHandSize(maxHandSize, modifier.source());
            } else if (modifier.effect() instanceof OpponentMaxHandSizeEffect handSizeEffect) {
                maxHandSize = handSizeEffect.applyToMaximumHandSize(maxHandSize, gameData,
                        modifier.sourceControllerId());
            }
        }
        return maxHandSize;
    }

    /** Orders hand-size effects like {@link #computeMaxHandSize}: by timestamp, then battlefield position. */
    private record HandSizeStamp(long timestamp, int position) {
        boolean isNewerThan(HandSizeStamp other) {
            return timestamp != other.timestamp ? timestamp > other.timestamp : position > other.position;
        }
    }

    private int handSizePosition(GameData gameData, Permanent target) {
        int position = 0;
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(otherPlayerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                if (perm == target) {
                    return position;
                }
                position++;
            }
        }
        return position;
    }

    /** The newest effect that sets this player's maximum hand size to a number, or null if none. */
    private HandSizeStamp latestHandSizeSettingStamp(GameData gameData, UUID playerId) {
        HandSizeStamp latest = null;
        for (UUID otherPlayerId : gameData.orderedPlayerIds) {
            boolean own = otherPlayerId.equals(playerId);
            List<Permanent> battlefield = gameData.playerBattlefields.get(otherPlayerId);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (CardEffect effect : unconditionalPrintedStaticEffects(gameData, perm)) {
                    if (own ? effect instanceof ControllerMaxHandSizeEffect
                            : effect instanceof OpponentMaxHandSizeEffect) {
                        HandSizeStamp stamp = new HandSizeStamp(perm.getTimestamp(), handSizePosition(gameData, perm));
                        if (latest == null || stamp.isNewerThan(latest)) {
                            latest = stamp;
                        }
                    }
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (FloatingContinuousEffect floating : gameData.floatingEffects) {
                if (playerId.equals(floating.affectedPlayerId())
                        && (floating.effect() instanceof ControllerMaxHandSizeEffect
                        || floating.effect() instanceof OpponentMaxHandSizeEffect)) {
                    HandSizeStamp stamp = new HandSizeStamp(floating.timestamp(), Integer.MAX_VALUE);
                    if (latest == null || stamp.isNewerThan(latest)) {
                        latest = stamp;
                    }
                }
            }
        }
        return latest;
    }

    private List<CardEffect> unconditionalPrintedStaticEffects(GameData gameData, Permanent perm) {
        if (perm.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
            return List.of();
        }
        return perm.getCard().getStaticEffects().stream()
                .filter(effect -> !(effect instanceof ConditionalEffect))
                .toList();
    }

    private record HandSizeModifier(CardEffect effect, Permanent source, long timestamp, int order, UUID sourceControllerId) {}

    /**
     * Checks whether the given player's hand size is unlimited, either via
     * a {@link GrantNoMaximumHandSizeEffect} floating effect on the player, by
     * controlling a permanent with {@link NoMaximumHandSizeEffect} (e.g. Spellbook),
     * or by any player controlling a permanent with
     * {@link PlayersHaveNoMaximumHandSizeEffect} (e.g. Anvil of Bogardan).
     *
     * @param gameData the current game state
     * @param playerId the player to check
     * @return {@code true} if the player has no maximum hand size
     */
    public boolean hasNoMaximumHandSize(GameData gameData, UUID playerId) {
        List<FloatingContinuousEffect> grants =
                gameData.floatingEffectsOnPlayer(playerId, GrantNoMaximumHandSizeEffect.class);
        if (!grants.isEmpty()) {
            // Effects on game rules apply in timestamp order: a hand-size setter that began after the newest grant
            // overrides it (CR 613.11).
            long grantedAt = grants.get(grants.size() - 1).timestamp();
            HandSizeStamp setterStamp = latestHandSizeSettingStamp(gameData, playerId);
            if (setterStamp == null || new HandSizeStamp(grantedAt, Integer.MAX_VALUE).isNewerThan(setterStamp)) {
                return true;
            }
        }
        synchronized (gameData.emblems) {
            for (var emblem : gameData.emblems) {
                if (playerId.equals(emblem.controllerId())
                        && emblem.staticEffects().stream().anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf != null) {
            synchronized (gameData.floatingEffects) {
                for (FloatingContinuousEffect fe : gameData.floatingEffects) {
                    if (fe.effect() instanceof NoMaximumHandSizeEffect
                            && playerId.equals(fe.affectedPlayerId())
                            && bf.stream().anyMatch(perm -> perm.getId().equals(fe.sourcePermanentId()))) {
                        return true;
                    }
                }
            }
            HandSizeStamp noMaximumStamp = null;
            for (Permanent perm : bf) {
                if (gameQueryService != null && gameQueryService.hasLostPrintedAbilities(gameData, perm)) {
                    continue;
                }
                List<CardEffect> staticEffects = gameQueryService == null
                        ? perm.getCard().getStaticEffects()
                        : gameQueryService.getActiveStaticEffects(gameData, perm, playerId);
                if (staticEffects.stream().anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                    HandSizeStamp stamp = new HandSizeStamp(perm.getTimestamp(), handSizePosition(gameData, perm));
                    if (noMaximumStamp == null || stamp.isNewerThan(noMaximumStamp)) {
                        noMaximumStamp = stamp;
                    }
                }
            }
            // Effects that set the maximum hand size apply in timestamp order, so a later "your maximum
            // hand size is N" overrides an earlier "no maximum hand size" (CR 613.7).
            if (noMaximumStamp != null) {
                HandSizeStamp setterStamp = latestHandSizeSettingStamp(gameData, playerId);
                if (setterStamp == null || noMaximumStamp.isNewerThan(setterStamp)) {
                    return true;
                }
            }
        }
        // Computing a static bonus can evaluate a condition that asks about maximum hand size.
        // The nested query still sees printed effects, but must not recursively compute bonuses.
        if (bf != null && gameQueryService != null && !checkingGrantedHandSizeEffects.get()) {
            checkingGrantedHandSizeEffects.set(true);
            try {
                for (Permanent perm : bf) {
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus != null && staticBonus.grantedEffects().stream()
                            .anyMatch(NoMaximumHandSizeEffect.class::isInstance)) {
                        return true;
                    }
                }
            } finally {
                checkingGrantedHandSizeEffects.remove();
            }
        }
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            if (battlefield.getValue() == null) {
                continue;
            }
            for (Permanent perm : battlefield.getValue()) {
                // A face-down or ability-losing permanent has no printed static abilities (CR 708.2).
                List<CardEffect> staticEffects = gameQueryService == null
                        ? perm.getCard().getStaticEffects()
                        : gameQueryService.getActiveStaticEffects(gameData, perm, battlefield.getKey());
                if (staticEffects.stream().anyMatch(PlayersHaveNoMaximumHandSizeEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        if (gameData.planechase != null) {
            for (var planarObject : gameData.planechase.faceUp) {
                if (planarObject.getCard().getStaticEffects().stream()
                        .anyMatch(PlayersHaveNoMaximumHandSizeEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }
}
