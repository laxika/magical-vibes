package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.rules.TapEvent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.AllPermanentsUntapDuringEachPlayersUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.effect.StorageMatrixEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.UntapStepReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.UntapPreventionSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the untap step (CR 502): cleaning stale untap-prevention locks,
 * untapping the active player's permanents (respecting "doesn't untap" effects),
 * queuing may-not-untap choices, clearing summoning sickness, and handling
 * "untap during each other player's untap step" effects (e.g. Seedborn Muse).
 *
 * <p>Extracted from {@code TurnProgressionService} to isolate untap-step logic
 * into a focused service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UntapStepService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final PhasingService phasingService;
    private final PermanentRemovalService permanentRemovalService;
    private final UntapPreventionSupport untapPreventionSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final DayNightService dayNightService;
    private final SubjectResolutionService subjectResolutionService;

    public void snapshotUntappedLandsAtTurnStart(GameData gameData, UUID activePlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        int count = battlefield == null ? 0 : (int) battlefield.stream()
                .filter(permanent -> !permanent.isTapped())
                .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                .count();
        gameData.untappedLandsAtTurnStart.put(activePlayerId, count);
    }

    /**
     * Performs the untap step for the active player.
     *
     * <ol>
     *   <li>Removes stale untap-prevention locks whose source permanent has left
     *       the battlefield or is no longer tapped.</li>
     *   <li>Untaps each of the active player's permanents unless it has a self-scoped
     *       {@link DoesntUntapEffect}, an attached (enchanted-scope) {@link DoesntUntapEffect},
     *       or an active untap lock.</li>
     *   <li>Queues a {@code PendingMayAbility} for each tapped permanent with
     *       {@link MayNotUntapDuringUntapStepEffect}, letting the controller choose.</li>
     *   <li>Clears summoning sickness and loyalty-ability-used flags.</li>
     *   <li>Untaps non-active players' permanents that have an
     *       {@link UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect}
     *       (e.g. Seedborn Muse).</li>
     * </ol>
     *
     * @param gameData       the current game state to modify
     * @param activePlayerId the player whose untap step is being processed
     */
    public void untapPermanents(GameData gameData, UUID activePlayerId) {
        untapPermanents(gameData, activePlayerId, null);
    }

    /**
     * Performs the untap step, optionally restricting which of the active player's permanents may
     * untap to those matching {@code restrictPredicate} (Storage Matrix). A {@code null} predicate
     * means no restriction (the normal untap step). The restriction applies only to the active
     * player's own permanents, not to "untap during each other player's step" effects (Seedborn
     * Muse), which untap during a different player's untap step and so are unaffected.
     *
     * @param restrictPredicate only permanents matching this untap; {@code null} = untap all
     */
    public void untapPermanents(GameData gameData, UUID activePlayerId, PermanentPredicate restrictPredicate) {
        untapPermanents(gameData, activePlayerId, restrictPredicate, false, null, null);
    }

    /**
     * Performs the untap step, optionally skipping the untapping entirely (Savor the Moment's "skip the
     * untap step of that turn"). When {@code skipUntapStep} is true, none of the active player's
     * permanents untap and no Seedborn-Muse-style cross-player untap or may-not-untap choices occur,
     * but summoning sickness and loyalty-activation flags are still cleared so the player can still
     * attack with and use creatures they already controlled.
     *
     * @param skipUntapStep {@code true} to keep every permanent tapped through this untap step
     */
    public void untapPermanents(GameData gameData, UUID activePlayerId, PermanentPredicate restrictPredicate,
                                boolean skipUntapStep) {
        untapPermanents(gameData, activePlayerId, restrictPredicate, skipUntapStep, null, null);
    }

    /**
     * Performs the untap step, restricting the active player's untaps under a Static-Orb-style lock:
     * of the permanents matching {@code staticOrbFilter}, only the explicitly chosen
     * {@code chosenUntapIds} untap (Static Orb: the player picked up to two; Stoic Angel: up to one
     * creature). A {@code null} filter means every permanent counts against the cap (Static Orb);
     * permanents the filter excludes untap normally. All other untap-step bookkeeping (summoning
     * sickness, skip counters, Seedborn Muse) proceeds normally.
     */
    public void untapChosenPermanents(GameData gameData, UUID activePlayerId, Set<UUID> chosenUntapIds,
                                      PermanentPredicate staticOrbFilter) {
        untapPermanents(gameData, activePlayerId, null, false, chosenUntapIds, staticOrbFilter);
    }

    private void untapPermanents(GameData gameData, UUID activePlayerId, PermanentPredicate restrictPredicate,
                                 boolean skipUntapStep, Set<UUID> chosenUntapIds, PermanentPredicate staticOrbFilter) {
        if (!gameData.additionalBeginningPhaseUntapInProgress) {
            snapshotUntappedLandsAtTurnStart(gameData, activePlayerId);
            // Loyalty activation limits reset each turn for every planeswalker, including one an opponent
            // controls and activated during its controller's previous turn.
            gameData.playerBattlefields.values().forEach(battlefield -> battlefield.forEach(p -> {
                p.setLoyaltyActivationsThisTurn(0);
            }));
        }
        String activePlayerName = gameData.playerIdToName.get(activePlayerId);
        gameData.untapStepPlayerId = activePlayerId;
        gameData.untapStepUntappedPermanentCount = 0;

        List<Permanent> activeBattlefield = gameData.playerBattlefields.get(activePlayerId);
        if (activeBattlefield != null) {
            activeBattlefield.forEach(p -> p.setUntappedAtTurnStart(!p.isTapped()));
        }

        if (skipUntapStep) {
            List<Permanent> ownBattlefield = activeBattlefield;
            if (ownBattlefield != null) {
                ownBattlefield.forEach(p -> {
                    // A wholly skipped step does not consume a restriction on the next untap step.
                    if (!gameData.additionalBeginningPhaseUntapInProgress) {
                        p.setSummoningSick(false);
                    }
                    p.setLoyaltyActivationsThisTurn(0);
                    });
            }
            // Nor does it count as that player's untap step for "your next untap step" restrictions.
            gameData.untapStepPlayerId = null;
            String skipLog = activePlayerName + " skips their untap step.";
            gameLogService.append(gameData, GameLog.text(skipLog));
            log.info("Game {} - {} skips their untap step", gameData.id, activePlayerName);
            return;
        }

        // CR 502.1: phasing is the untap step's first turn-based action, resolved before anything
        // untaps (and skipped entirely along with the rest of the step above).
        if (activePlayerId.equals(gameData.untapPhasingAppliedPlayerId)) {
            gameData.untapPhasingAppliedPlayerId = null;
        } else {
            phasingService.applyPhasing(gameData, activePlayerId);
        }
        dayNightService.checkAtUntap(gameData, activePlayerId);

        Set<UUID> restrictedPermanentIds = new java.util.HashSet<>();
        // A "don't untap during that player's next untap step" rule registered on the active player (CR 611.2c): its
        // set's filter is judged now, against what that player controls (or, all-player, everyone's permanents).
        for (var restrictionEffect : gameData.nextUntapStepRestrictions(activePlayerId)) {
            var restriction = (SkipNextUntapEffect) restrictionEffect.effect();
            PermanentPredicate filter = ((PermanentSubject.ControlledBy) restriction.subject()).filter();
            if (restriction.allPlayersAtUntap()) {
                gameData.forEachPermanent((playerId, permanent) -> {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                        restrictedPermanentIds.add(permanent.getId());
                    }
                });
            } else {
                for (Permanent permanent : gameData.playerBattlefields.getOrDefault(activePlayerId, List.of())) {
                    if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                        restrictedPermanentIds.add(permanent.getId());
                    }
                }
            }
            if (restriction.untapSteps() > 1) {
                gameData.replaceFloatingEffect(restrictionEffect,
                        restrictionEffect.withEffect(restriction.withOneStepFewer()));
            } else {
                gameData.endFloatingEffect(restrictionEffect);
            }
        }

        // A permanent that phased out is treated as though it does not exist (CR 702.26b), so an
        // attachment that was kept from following it out (Spatial Binding) is now attached to
        // nothing and belongs in its owner's graveyard (CR 303.4c) — the official Spatial Binding
        // ruling says immediately. This is a state-based action, but nothing runs the SBA loop
        // between the untap step and the next stack resolution, so the sweep is run here; every
        // other way a host leaves the battlefield already sweeps at its own call site.
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> mayNotUntapPermanents = new ArrayList<>();
        List<Permanent> untappedDuringStep = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        // CR 502.3: the untaps happen simultaneously, so a lock is judged against the state at the
        // start of the step. An untap that releases a lock (Kill Switch) must not free the locked
        // permanents that happen to be processed after it.
        Set<UUID> untapLockedAtStart = battlefield == null ? Set.of() : battlefield.stream()
                .filter(p -> untapPreventionSupport.hasSourceLinkedUntapLock(gameData, p))
                .map(Permanent::getId)
                .collect(java.util.stream.Collectors.toSet());
        if (battlefield != null) {
            battlefield.forEach(p -> {
                // An attached() DoesntUntapEffect on an Aura or Equipment keeps the host tapped.
                boolean hasAttachedDoesntUntap = hasAttachedDoesntUntap(gameData, p);
                boolean hasSelfDoesntUntap = untapPreventionSupport.hasActiveSelfDoesntUntap(gameData, p);
                boolean hasMayNotUntap = p.isTapped() && gameQueryService.hasActiveStaticEffect(
                        gameData, p, MayNotUntapDuringUntapStepEffect.class);
                boolean hasUntapLock = untapLockedAtStart.contains(p.getId());
                boolean skipsNextUntap = !gameData.nextUntapStepSkips(p.getId(), activePlayerId).isEmpty();
                // A global static (e.g. Marble Titan) can lock this permanent based on a predicate.
                boolean hasMatchingDoesntUntap = matchingStaticPreventsUntap(gameData, p);
                // Depletion lands (Land Cap): a self-scoped static lock conditioned on a counter.
                boolean hasCounterLock = counterLockPreventsUntap(gameData, p);
                boolean cannotBecomeUntapped = gameQueryService.cantBecomeUntapped(gameData, p);

                boolean blockedByStorageMatrix = restrictPredicate != null
                        && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, restrictPredicate);
                // Static Orb / Stoic Angel: only the permanents the active player chose untap this
                // step. When a filter is present (Stoic Angel: creatures), permanents the filter
                // excludes are not subject to the cap and untap normally.
                boolean subjectToStaticOrb = staticOrbFilter == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, staticOrbFilter);
                boolean blockedByStaticOrb = chosenUntapIds != null && subjectToStaticOrb
                        && !chosenUntapIds.contains(p.getId());

                if (skipsNextUntap) {
                    // Each "doesn't untap during its next untap step" effect that applies is used up by this step
                    // (Vorinclex, exert).
                    gameData.useNextUntapStepSkips(p.getId(), activePlayerId);
                } else if (blockedByStorageMatrix || blockedByStaticOrb || restrictedPermanentIds.contains(p.getId())) {
                    // Storage Matrix / untap cap: not selected to untap — stays tapped this step
                } else if (cannotBecomeUntapped) {
                    // A hard prevention effect such as Blossombind also suppresses optional untap choices.
                } else if (hasMayNotUntap && !hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasUntapLock
                        && !hasMatchingDoesntUntap && !hasCounterLock) {
                    // Present choice to controller later — skip untap for now
                    mayNotUntapPermanents.add(p);
                } else if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasUntapLock
                        && !hasMatchingDoesntUntap && !hasCounterLock) {
                    // Freyalise's Winds: the untap is replaced by removing all counters of the
                    // named type, so the permanent stays tapped this step. An untapped permanent
                    // is not untapping, so no untap replacement applies to it.
                    boolean replaced = p.isTapped() && replaceUntap(gameData, p, activePlayerId);
                    if (!replaced && tapUntapSupport.untapPermanent(gameData, p)) {
                        untappedDuringStep.add(p);
                    }
                }
                // An additional beginning phase is not a new turn, so it does not end summoning sickness.
                if (!gameData.additionalBeginningPhaseUntapInProgress) {
                    p.setSummoningSick(false);
                }
                p.setLoyaltyActivationsThisTurn(0);
            });
        }
        gameData.untapStepUntappedPermanentCount = untappedDuringStep.size();

        // Undiscovered Paradise: return flagged permanents to hand as the active player untaps
        // (even if some effect prevented that permanent from untapping).
        returnPermanentsFlaggedForUntap(gameData, activePlayerId);

        String untapLog = activePlayerName + " untaps their permanents.";
        gameLogService.append(gameData, GameLog.text(untapLog));
        log.info("Game {} - {} untaps their permanents", gameData.id, activePlayerName);

        untapAllPermanentsDuringEachPlayersUntapStep(gameData, activePlayerId, restrictedPermanentIds);

        // Queue may-not-untap choices for tapped permanents with MayNotUntapDuringUntapStepEffect
        for (Permanent p : mayNotUntapPermanents) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    p.getCard(),
                    activePlayerId,
                    List.of(new MayNotUntapDuringUntapStepEffect()),
                    "Untap " + FaceDownVisibility.publicName(gameData, p) + "?"
            ));
        }

        untapDuringOtherPlayersStep(gameData, activePlayerId, restrictedPermanentIds);
    }

    private void untapAllPermanentsDuringEachPlayersUntapStep(GameData gameData, UUID activePlayerId,
                                                               Set<UUID> restrictedPermanentIds) {
        if (!allPermanentsUntapDuringEachPlayersUntapStepApplies(gameData)) {
            return;
        }

        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) {
                return;
            }
            playerBattlefield.stream()
                    .filter(permanent -> !restrictedPermanentIds.contains(permanent.getId()))
                    .forEach(permanent -> tapUntapSupport.untapPermanent(gameData, permanent));
        });
    }

    private boolean allPermanentsUntapDuringEachPlayersUntapStepApplies(GameData gameData) {
        if (gameData.planechase == null) {
            return false;
        }
        return gameData.planechase.faceUp.stream()
                .anyMatch(object -> object.getCard().getStaticEffects().stream()
                        .anyMatch(effect -> effect instanceof AllPermanentsUntapDuringEachPlayersUntapStepEffect));
    }

    /**
     * Ends exert-style "won't untap during your next untap step" restrictions owed to
     * {@code activePlayerId}, wherever the permanent is now — they expire with that player's
     * untap step even if another player controls the permanent.
     */
    public void expireControllerStepUntapRestrictions(GameData gameData, UUID activePlayerId) {
        gameData.endNextUntapStepSkipsOf(activePlayerId);
    }

    /** Queues the batched untap-step triggers after all untap choices are complete. */
    public void finishUntapStep(GameData gameData, UUID activePlayerId) {
        if (activePlayerId.equals(gameData.untapStepPlayerId)) {
            expireControllerStepUntapRestrictions(gameData, activePlayerId);
            gameData.rulesEvents.occur(new TapEvent.UntappedDuringUntapStep(activePlayerId,
                    gameData.untapStepUntappedPermanentCount));
        }
        gameData.untapStepPlayerId = null;
        gameData.untapStepUntappedPermanentCount = 0;
    }

    /**
     * Returns {@code true} if any permanent (any controller) or face-up planar card carries a
     * {@link PlayersSkipUntapStepEffect}. While true, each player's untap step is skipped entirely
     * (no phasing, no untapping) — see {@link #untapPermanents} with {@code skipUntapStep=true}.
     */
    public boolean playersSkipUntapStepApplies(GameData gameData) {
        boolean planarEffectApplies = gameData.planechase != null && gameData.planechase.faceUp.stream()
                .anyMatch(object -> object.getCard().getStaticEffects().stream()
                        .anyMatch(effect -> effect instanceof PlayersSkipUntapStepEffect));
        return planarEffectApplies || gameData.anyPermanentMatches(p ->
                !gameQueryService.hasLostAllAbilities(gameData, p)
                        && p.getCard().getStaticEffects().stream()
                        .anyMatch(e -> e instanceof PlayersSkipUntapStepEffect));
    }

    /**
     * Applies the untap step's phasing, then reports whether a Storage Matrix restriction is in force
     * (see {@link #storageMatrixRestrictionApplies}). Used where the Storage Matrix choice is made
     * before the untap, so a phased-out Storage Matrix is already back when the player chooses.
     */
    public boolean phaseThenCheckStorageMatrixRestriction(GameData gameData, UUID activePlayerId) {
        // CR 502.1: phasing is the untap step's first turn-based action, so a phased-out Storage Matrix
        // phases in before the player chooses; the untap itself must not phase a second time.
        phasingService.applyPhasing(gameData, activePlayerId);
        gameData.untapPhasingAppliedPlayerId = activePlayerId;
        return storageMatrixRestrictionApplies(gameData, activePlayerId);
    }

    /**
     * Returns {@code true} if a Storage Matrix untap restriction is in force for the given active
     * player: some untapped permanent (any controller) carries a {@link StorageMatrixEffect} and
     * the active player has at least one tapped permanent to decide about. When there is nothing
     * tapped, the choice would have no observable effect and is skipped.
     */
    public boolean storageMatrixRestrictionApplies(GameData gameData, UUID activePlayerId) {
        boolean untappedMatrixPresent = gameData.anyPermanentMatches(p -> !p.isTapped()
                && gameQueryService.hasActiveStaticEffect(gameData, p, StorageMatrixEffect.class));
        if (!untappedMatrixPresent) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        return battlefield != null && battlefield.stream().anyMatch(Permanent::isTapped);
    }

    /**
     * Returns the currently-binding Static-Orb-style untap restriction for the given active player,
     * if any: an active {@link StaticOrbEffect} (its source untapped when the effect requires it)
     * whose filtered untap-candidate pool exceeds its {@code maxUntap} cap, so the active player must
     * choose. When the pool is at or below the cap the choice would have no observable effect and is
     * skipped. When several restrictions are active the first that binds is returned.
     */
    public java.util.Optional<StaticOrbEffect> bindingUntapRestriction(GameData gameData, UUID activePlayerId) {
        return bindingUntapRestrictions(gameData, activePlayerId).stream().findFirst();
    }

    /** All binding untap caps, retaining the strictest cap for each affected-permanent filter. */
    public List<StaticOrbEffect> bindingUntapRestrictions(GameData gameData, UUID activePlayerId) {
        List<StaticOrbEffect> active = new ArrayList<>();
        gameData.forEachPermanent((controllerId, p) -> {
            for (CardEffect e : p.getCard().getStaticEffects()) {
                if (e instanceof StaticOrbEffect orb
                        && !gameQueryService.hasLostPrintedAbilities(gameData, p)
                        && appliesToUntapStep(orb, activePlayerId, controllerId)
                        && (!orb.requiresUntappedSource() || !p.isTapped())) {
                    active.add(orb);
                }
            }
        });
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect e : emblem.staticEffects()) {
                if (e instanceof StaticOrbEffect orb
                        && !orb.requiresUntappedSource()
                        && appliesToUntapStep(orb, activePlayerId, emblem.controllerId())) {
                    active.add(orb);
                }
            }
        }
        var byFilter = new java.util.LinkedHashMap<PermanentPredicate, StaticOrbEffect>();
        for (StaticOrbEffect effect : active) {
            byFilter.merge(effect.filter(), effect, (first, second) ->
                    first.maxUntap() <= second.maxUntap() ? first : second);
        }
        return byFilter.values().stream().filter(effect ->
                staticOrbUntapCandidates(gameData, activePlayerId, effect).size() > effect.maxUntap()).toList();
    }

    private boolean appliesToUntapStep(StaticOrbEffect effect, UUID activePlayerId, UUID sourceControllerId) {
        if (effect.controllerOnly()) {
            return activePlayerId.equals(sourceControllerId);
        }
        return !effect.opponentsOnly() || !activePlayerId.equals(sourceControllerId);
    }

    /**
     * Returns {@code true} if a Static-Orb-style untap restriction is currently in force for the
     * given active player (see {@link #bindingUntapRestriction}).
     */
    public boolean staticOrbRestrictionApplies(GameData gameData, UUID activePlayerId) {
        return bindingUntapRestriction(gameData, activePlayerId).isPresent();
    }

    /**
     * Returns the ids of the active player's permanents that would untap during a normal untap step
     * and that match the given restriction's {@code filter} — the pool the player picks up to the
     * cap from when the restriction applies. Permanents that would not untap anyway (self/attached
     * "doesn't untap", untap locks, a pending skip, or a global "doesn't untap" lock), or that the
     * filter excludes, never count against the cap and are omitted. A permanent with a "you may
     * choose not to untap" ability still counts: the player may pick it, then makes that choice.
     */
    public List<UUID> staticOrbUntapCandidates(GameData gameData, UUID activePlayerId,
                                               StaticOrbEffect effect, PermanentPredicate restrictPredicate) {
        return staticOrbUntapCandidates(gameData, activePlayerId, effect).stream()
                .filter(id -> predicateEvaluationService.matchesPermanentPredicate(gameData,
                        gameQueryService.findPermanentById(gameData, id), restrictPredicate))
                .toList();
    }

    public List<UUID> staticOrbUntapCandidates(GameData gameData, UUID activePlayerId, StaticOrbEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return List.of();
        }
        List<UUID> candidates = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (!p.isTapped() || !gameData.nextUntapStepSkips(p.getId(), activePlayerId).isEmpty()) {
                continue;
            }
            if (effect.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.filter())) {
                continue;
            }
            boolean hasAttachedDoesntUntap = hasAttachedDoesntUntap(gameData, p);
            boolean hasSelfDoesntUntap = untapPreventionSupport.hasActiveSelfDoesntUntap(gameData, p);
            boolean hasUntapLock = untapPreventionSupport.hasSourceLinkedUntapLock(gameData, p);
            boolean hasMatchingDoesntUntap = matchingStaticPreventsUntap(gameData, p);
            boolean hasCounterLock = counterLockPreventsUntap(gameData, p);
            boolean cannotBecomeUntapped = gameQueryService.cantBecomeUntapped(gameData, p);
            if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap
                    && !hasUntapLock && !hasMatchingDoesntUntap
                    && !hasCounterLock && !cannotBecomeUntapped) {
                candidates.add(p.getId());
            }
        }
        return candidates;
    }

    /**
     * Applies an untap-step replacement from a battlefield or face-up planar source.
     *
     * @return {@code true} if the untap was replaced, {@code false} if the permanent should untap
     */
    /**
     * Applies any untap replacement that would stop {@code permanent} from untapping, such as
     * Freyalise's Winds removing its wind counters, for an untap that happens outside the untap
     * step sequence (a "may choose not to untap" permanent the controller chose to untap).
     *
     * @return {@code true} if an untap replacement applied and the permanent stays tapped
     */
    public boolean replaceUntapIfApplicable(GameData gameData, Permanent permanent, UUID permanentControllerId) {
        return replaceUntap(gameData, permanent, permanentControllerId);
    }

    private boolean replaceUntap(GameData gameData, Permanent permanent, UUID permanentControllerId) {
        final boolean[] replaced = {false};
        gameData.forEachBattlefield((sourceControllerId, battlefield) -> {
            if (replaced[0]) {
                return;
            }
            for (Permanent source : battlefield) {
                if (gameQueryService.hasLostAllAbilities(gameData, source)) {
                    continue;
                }
                if (applyUntapReplacement(gameData, permanent, permanentControllerId,
                        source.getCard(), sourceControllerId)) {
                    replaced[0] = true;
                    return;
                }
            }
        });
        if (!replaced[0] && gameData.planechase != null) {
            UUID sourceControllerId = gameData.planechase.controllerId;
            for (PlanarObject source : gameData.planechase.faceUp) {
                if (applyUntapReplacement(gameData, permanent, permanentControllerId,
                        source.getCard(), sourceControllerId)) {
                    replaced[0] = true;
                    break;
                }
            }
        }
        return replaced[0];
    }

    private boolean applyUntapReplacement(GameData gameData, Permanent permanent,
                                          UUID permanentControllerId, Card sourceCard,
                                          UUID sourceControllerId) {
        for (CardEffect effect : sourceCard.getStaticEffects()) {
            if (!(effect instanceof UntapStepReplacementEffect replacement)) {
                continue;
            }
            if (replacement.sourceControllerOnly()
                    && !Objects.equals(sourceControllerId, permanentControllerId)) {
                continue;
            }
            PermanentPredicate filter = replacement.filter();
            if (filter != null && !predicateEvaluationService.matchesPermanentPredicate(
                    permanent, filter,
                    FilterContext.of(gameData)
                            .withSourceCardId(sourceCard.getId())
                            .withSourceControllerId(sourceControllerId))) {
                continue;
            }
            if (!replacement.removesCounters()) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, null, permanent,
                        replacement.counterType(), replacement.replacementCount());
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                        " doesn't untap; counters are put on it instead."));
                return true;
            }
            if (permanent.getCounterCount(replacement.counterType()) <= 0) {
                continue;
            }
            int removed = permanent.getCounterCount(replacement.counterType());
            permanent.setCounterCount(replacement.counterType(), 0);
            if (replacement.counterType() == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, removed);
            }
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    " doesn't untap; its counters are removed instead."));
            log.info("Game {} - {} does not untap; counters {} removed instead",
                    gameData.id, permanent.getCard().getName(), replacement.counterType());
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if any permanent on any battlefield carries a
     * {@link MatchingPermanentsDoesntUntapEffect} whose filter matches the given permanent
     * (e.g. Marble Titan locking every creature with power 3 or greater, including its own).
     */
    /**
     * Returns {@code true} if the permanent carries a {@link DoesntUntapWithCounterEffect} and
     * currently has at least one counter of that type on it (Land Cap and the other Ice Age
     * depletion lands).
     */
    private boolean counterLockPreventsUntap(GameData gameData, Permanent permanent) {
        // A creature that has lost all abilities (Darksteel Mutation) loses its own printed and granted
        // "doesn't untap" locks too; a face-down permanent has no abilities at all. An aura-granted lock
        // comes from the aura, which is unaffected.
        boolean ownLockApplies = !permanent.isFaceDown()
                && (!gameQueryService.hasLostAllAbilities(gameData, permanent)
                && hasActiveCounterLock(gameData, permanent, permanent.getCard().getStaticEffects(), permanent)
                // Granted, not printed: Mindbender Spores gives the creature it blocks
                // "doesn't untap during your untap step if it has a fungus counter on it".
                || hasActiveCounterLock(gameData, permanent,
                        gameQueryService.getGrantedEffects(gameData, permanent), permanent));
        return ownLockApplies || gameData.anyPermanentMatches(source -> source.isAttached()
                        && source.getAttachedTo().equals(permanent.getId())
                        && (hasActiveCounterLock(gameData, source, source.getCard().getStaticEffects(), permanent)
                        || hasActiveCounterLock(gameData, source,
                                gameQueryService.getGrantedEffects(gameData, source), permanent)));
    }

    /** Whether one of {@code source}'s counter locks names {@code permanent} and its counter bearer has the counter. */
    private boolean hasActiveCounterLock(GameData gameData, Permanent source, List<CardEffect> effects,
                                         Permanent permanent) {
        return effects.stream()
                .anyMatch(e -> e instanceof DoesntUntapWithCounterEffect lock
                        && namesOwnOrHost(source, lock.subject(), permanent)
                        && subjectResolutionService.permanents(gameData, source, lock.counterBearer())
                                .stream()
                                .anyMatch(bearer -> bearer.getCounterCount(lock.counterType()) > 0));
    }

    /**
     * Whether an untap lock of {@code source} with {@code subject} ({@code source()} or {@code attached()}) names
     * {@code permanent}: the source itself, or what it is attached to.
     */
    private static boolean namesOwnOrHost(Permanent source, PermanentSubject subject, Permanent permanent) {
        return switch (subject) {
            case PermanentSubject.Source ignored -> source.getId().equals(permanent.getId());
            case PermanentSubject.Attached ignored -> permanent.getId().equals(source.getAttachedTo());
            default -> false;
        };
    }

    /** Whether an Aura or Equipment attached to {@code permanent} has an {@code attached()} {@link DoesntUntapEffect}. */
    private boolean hasAttachedDoesntUntap(GameData gameData, Permanent permanent) {
        return gameQueryService.hasAuraWithEffect(gameData, permanent,
                e -> e instanceof DoesntUntapEffect lock && lock.subject() instanceof PermanentSubject.Attached);
    }

    private boolean matchingStaticPreventsUntap(GameData gameData, Permanent permanent) {
        return gameData.anyPermanentMatches(source -> !gameQueryService.hasLostAllAbilities(gameData, source)
                && source.getCard().getStaticEffects().stream()
                .anyMatch(e -> e instanceof MatchingPermanentsDoesntUntapEffect lock
                        // Source-relative filters (An-Zerrin Ruins' chosen creature type) need the
                        // locking permanent as the filter's source, not just the game state.
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, lock.filter(),
                                FilterContext.of(gameData).withSourceCardId(source.getCard().getId()))));
    }

    /**
     * "Untap [these] during each other player's untap step" (Seedborn Muse, Urban Burgeoning, Bender's Waterskin): each
     * permanent the subject of such a static ability names untaps if a player other than the active one controls it.
     * A set ({@code controlledBy(you(), filter)}, from a permanent or an emblem of that player) untaps that player's
     * matching permanents and is logged once per player; a named permanent (the source itself, the Aura's host) is
     * logged by name if it was tapped. An Aura's grant lives on its host, so the host's controller, not the Aura's,
     * decides whose untap step is "another player's". A permanent a "next untap step" rule holds stays tapped.
     */
    private void untapDuringOtherPlayersStep(GameData gameData, UUID activePlayerId, Set<UUID> restrictedPermanentIds) {
        List<CrossPlayerUntap> untaps = collectCrossPlayerUntaps(gameData, TurnStep.UNTAP);
        if (untaps.isEmpty()) {
            return;
        }
        java.util.Map<UUID, Boolean> setPlayersUnfiltered = new java.util.HashMap<>();
        for (CrossPlayerUntap untap : untaps) {
            if (!(untap.effect().subject() instanceof PermanentSubject.ControlledBy set)
                    || untap.controllerId().equals(activePlayerId)) {
                continue;
            }
            setPlayersUnfiltered.merge(untap.controllerId(), set.filter() instanceof PermanentTruePredicate,
                    Boolean::logicalOr);
            for (Permanent permanent : namedBy(gameData, untap)) {
                if (!restrictedPermanentIds.contains(permanent.getId())) {
                    tapUntapSupport.untapPermanent(gameData, permanent);
                }
            }
        }
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            Boolean unfiltered = setPlayersUnfiltered.get(playerId);
            if (unfiltered == null) {
                return;
            }
            String playerName = gameData.playerIdToName.get(playerId);
            if (unfiltered) {
                String seedbornLog = playerName + " untaps their permanents due to Seedborn Muse.";
                gameLogService.append(gameData, GameLog.text(seedbornLog));
                log.info("Game {} - {} untaps permanents due to Seedborn Muse", gameData.id, playerName);
            } else {
                String filteredLog = playerName + " untaps some permanents during opponent's untap step.";
                gameLogService.append(gameData, GameLog.text(filteredLog));
                log.info("Game {} - {} untaps filtered permanents during opponent's untap step", gameData.id, playerName);
            }
        });
        for (CrossPlayerUntap untap : untaps) {
            if (untap.effect().subject() instanceof PermanentSubject.ControlledBy) {
                continue;
            }
            for (Permanent permanent : namedBy(gameData, untap)) {
                UUID controllerId = gameData.findControllerOf(permanent);
                if (controllerId == null || controllerId.equals(activePlayerId) || !permanent.isTapped()
                        || restrictedPermanentIds.contains(permanent.getId())) {
                    continue;
                }
                tapUntapSupport.untapPermanent(gameData, permanent);
                String logLine = gameData.playerIdToName.get(controllerId) + " untaps " + FaceDownVisibility.publicName(gameData, permanent)
                        + " during another player's untap step.";
                gameLogService.append(gameData, GameLog.text(logLine));
                log.info("Game {} - {} untaps {} during another player's untap step",
                        gameData.id, controllerId, permanent.getCard().getName());
            }
        }
    }

    private List<Permanent> namedBy(GameData gameData, CrossPlayerUntap untap) {
        return untap.emblem()
                ? subjectResolutionService.permanents(gameData, untap.source(), untap.controllerId(),
                        untap.effect().subject())
                : subjectResolutionService.permanents(gameData, untap.source(), untap.effect().subject());
    }

    /**
     * Every active cross-player untap ability of {@code step}: on a permanent of any battlefield (a condition is
     * checked, and the source must still have the ability: a set's or the source's own, not an Aura's grant to its
     * host) and on an emblem (a set only).
     */
    private List<CrossPlayerUntap> collectCrossPlayerUntaps(GameData gameData, TurnStep step) {
        List<CrossPlayerUntap> result = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            for (CardEffect effect : permanent.getCard().getStaticEffects()) {
                collectActiveCrossPlayerUntap(gameData, permanent, controllerId, step, effect, result);
            }
        });
        for (Emblem emblem : gameData.emblems) {
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect
                        && configuredEffect.step() == step
                        && configuredEffect.subject() instanceof PermanentSubject.ControlledBy) {
                    result.add(new CrossPlayerUntap(new Permanent(emblem.sourceCard()), emblem.controllerId(),
                            configuredEffect, true));
                }
            }
        }
        return result;
    }

    private void collectActiveCrossPlayerUntap(GameData gameData, Permanent source, UUID controllerId,
                                               TurnStep step, CardEffect effect, List<CrossPlayerUntap> result) {
        if (effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect) {
            boolean active = switch (configuredEffect.subject()) {
                case PermanentSubject.Attached ignored -> true;
                case PermanentSubject.Source ignored -> !gameQueryService.hasLostPrintedAbilities(gameData, source);
                default -> !gameQueryService.hasLostAllAbilities(gameData, source);
            };
            if (configuredEffect.step() == step && active) {
                result.add(new CrossPlayerUntap(source, controllerId, configuredEffect, false));
            }
            return;
        }
        if (effect instanceof ConditionalEffect conditional
                && !gameQueryService.hasLostAllAbilities(gameData, source)
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStaticEffect(source, controllerId))) {
            collectActiveCrossPlayerUntap(gameData, source, controllerId, step, conditional.wrapped(), result);
        }
    }

    /** A cross-player untap ability, its source (an emblem's source card for an emblem) and its controller. */
    private record CrossPlayerUntap(
            Permanent source,
            UUID controllerId,
            UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect effect,
            boolean emblem) {
    }

    /**
     * Returns every permanent whose "return this to its owner's hand during your next untap step" rider
     * (Undiscovered Paradise) was set up by the active player, wherever it is controlled now. The rider
     * belongs to the player who set it up, so a control change does not move it to the new controller's
     * untap step. Happens as that player untaps their permanents, including when the permanent itself
     * did not untap.
     */
    private void returnPermanentsFlaggedForUntap(GameData gameData, UUID activePlayerId) {
        List<Permanent> returning = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            battlefield.stream()
                    .filter(permanent -> activePlayerId.equals(permanent.getReturnToHandAtNextUntapPlayerId()))
                    .forEach(returning::add);
        }
        if (returning.isEmpty()) {
            return;
        }
        for (Permanent permanent : returning) {
            permanent.setReturnToHandAtNextUntapPlayerId(null);
            permanentRemovalService.removePermanentToHand(gameData, permanent);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
