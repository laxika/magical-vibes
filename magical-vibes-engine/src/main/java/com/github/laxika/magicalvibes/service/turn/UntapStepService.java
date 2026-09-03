package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.MayNotUntapDuringUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PlayersSkipUntapStepEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersInsteadOfUntappingEffect;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.effect.StorageMatrixEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.UntapPreventionSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TapUntapSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    private final TriggerCollectionService triggerCollectionService;
    private final DayNightService dayNightService;

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
                    // Permanents stay tapped, but a queued "skip next untap" is still consumed (this
                    // untap step would have been its chance to untap) and summoning sickness clears.
                    if (p.getSkipUntapCount() > 0) {
                        p.setSkipUntapCount(p.getSkipUntapCount() - 1);
                    }
                    p.setSummoningSick(false);
                    p.setLoyaltyActivationsThisTurn(0);
                    p.setExtraLoyaltyActivationsThisTurn(0);
                });
            }
            String skipLog = activePlayerName + " skips their untap step.";
            gameLogService.append(gameData, GameLog.text(skipLog));
            log.info("Game {} - {} skips their untap step", gameData.id, activePlayerName);
            return;
        }

        // CR 502.1: phasing is the untap step's first turn-based action, resolved before anything
        // untaps (and skipped entirely along with the rest of the step above).
        phasingService.applyPhasing(gameData, activePlayerId);
        dayNightService.checkAtUntap(gameData, activePlayerId);

        // A permanent that phased out is treated as though it does not exist (CR 702.26b), so an
        // attachment that was kept from following it out (Spatial Binding) is now attached to
        // nothing and belongs in its owner's graveyard (CR 303.4c) — the official Spatial Binding
        // ruling says immediately. This is a state-based action, but nothing runs the SBA loop
        // between the untap step and the next stack resolution, so the sweep is run here; every
        // other way a host leaves the battlefield already sweeps at its own call site.
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Clean up stale untap-prevention locks on ALL battlefields before untapping.
        // A lock is stale if the source permanent is no longer on the battlefield or is no longer tapped.
        gameData.forEachPermanent((pid, p) -> {
            if (!p.getUntapPreventedByPermanentIds().isEmpty()) {
                p.getUntapPreventedByPermanentIds().removeIf(sourceId -> {
                    Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
                    return source == null || !source.isTapped();
                });
            }
            // Clean up "while source on battlefield" locks — only removed when source leaves battlefield.
            if (!p.getUntapPreventedWhileSourceOnBattlefieldIds().isEmpty()) {
                p.getUntapPreventedWhileSourceOnBattlefieldIds().removeIf(sourceId -> {
                    Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
                    return source == null;
                });
            }
        });

        // Untap all permanents for the new active player (skip those with "doesn't untap" effects)
        List<Permanent> mayNotUntapPermanents = new ArrayList<>();
        List<Permanent> untappedDuringStep = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield != null) {
            battlefield.forEach(p -> {
                // ENCHANTED-scope DoesntUntapEffect on an attached aura keeps the host tapped.
                boolean hasAttachedDoesntUntap = gameQueryService.hasAuraWithEffect(gameData, p, DoesntUntapEffect.class);
                boolean hasSelfDoesntUntap = untapPreventionSupport.hasActiveSelfDoesntUntap(gameData, p);
                boolean hasMayNotUntap = p.isTapped() && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
                boolean hasUntapLock = !p.getUntapPreventedByPermanentIds().isEmpty()
                        || !p.getUntapPreventedWhileSourceOnBattlefieldIds().isEmpty();
                boolean skipsNextUntap = p.getSkipUntapCount() > 0;
                // A global static (e.g. Marble Titan) can lock this permanent based on a predicate.
                boolean hasMatchingDoesntUntap = matchingStaticPreventsUntap(gameData, p);
                // Paralyzation counters (Dread Wight): doesn't untap during the untap step for as
                // long as it has such a counter — source-independent continuous rule.
                boolean hasParalyzationLock = p.getCounterCount(CounterType.PARALYZATION) > 0;
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
                    // Decrement skip counter but don't untap this step (e.g. Vorinclex)
                    p.setSkipUntapCount(p.getSkipUntapCount() - 1);
                } else if (blockedByStorageMatrix || blockedByStaticOrb) {
                    // Storage Matrix / untap cap: not selected to untap — stays tapped this step
                } else if (cannotBecomeUntapped) {
                    // A hard prevention effect such as Blossombind also suppresses optional untap choices.
                } else if (hasMayNotUntap) {
                    // Present choice to controller later — skip untap for now
                    mayNotUntapPermanents.add(p);
                } else if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasUntapLock
                        && !hasMatchingDoesntUntap && !hasParalyzationLock && !hasCounterLock) {
                    // Freyalise's Winds: the untap is replaced by removing all counters of the
                    // named type, so the permanent stays tapped this step.
                    if (!removeCountersInsteadOfUntapping(gameData, p)) {
                        if (tapUntapSupport.untapPermanent(gameData, p)) {
                            untappedDuringStep.add(p);
                        }
                    }
                }
                p.setSummoningSick(false);
                p.setLoyaltyActivationsThisTurn(0);
                p.setExtraLoyaltyActivationsThisTurn(0);
            });
        }
        gameData.untapStepUntappedPermanentCount = untappedDuringStep.size();

        // Undiscovered Paradise: return flagged permanents to hand as the active player untaps
        // (even if some effect prevented that permanent from untapping).
        returnPermanentsFlaggedForUntap(gameData, activePlayerId);

        String untapLog = activePlayerName + " untaps their permanents.";
        gameLogService.append(gameData, GameLog.text(untapLog));
        log.info("Game {} - {} untaps their permanents", gameData.id, activePlayerName);

        // Queue may-not-untap choices for tapped permanents with MayNotUntapDuringUntapStepEffect
        for (Permanent p : mayNotUntapPermanents) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    p.getCard(),
                    activePlayerId,
                    List.of(new MayNotUntapDuringUntapStepEffect()),
                    "Untap " + p.getCard().getName() + "?"
            ));
        }

        // Untap permanents for non-active players that have "untap during each other player's step" effects
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return;

            List<CrossPlayerUntap> untapEffects =
                    collectUntapOnEachOtherPlayersStepEffects(gameData, playerId, TurnStep.UNTAP);
            if (untapEffects.isEmpty()) return;

            boolean hasUnfilteredEffect = untapEffects.stream().anyMatch(e -> e.effect().filter() == null);

            for (Permanent p : playerBattlefield) {
                if (hasUnfilteredEffect || untapEffects.stream().anyMatch(e -> e.effect().filter() != null
                        && predicateEvaluationService.matchesPermanentPredicate(p, e.effect().filter(),
                        FilterContext.of(gameData)
                                .withSourceCardId(e.source().getCard().getId())
                                .withSourceControllerId(playerId)
                                .withSourcePermanentId(e.source().getId())))) {
                    tapUntapSupport.untapPermanent(gameData, p);
                }
            }

            String playerName = gameData.playerIdToName.get(playerId);
            if (hasUnfilteredEffect) {
                String seedbornLog = playerName + " untaps their permanents due to Seedborn Muse.";
                gameLogService.append(gameData, GameLog.text(seedbornLog));
                log.info("Game {} - {} untaps permanents due to Seedborn Muse", gameData.id, playerName);
            } else {
                String filteredLog = playerName + " untaps some permanents during opponent's untap step.";
                gameLogService.append(gameData, GameLog.text(filteredLog));
                log.info("Game {} - {} untaps filtered permanents during opponent's untap step", gameData.id, playerName);
            }
        });

        untapSelfPermanentsDuringOtherPlayersStep(gameData, activePlayerId);
        untapEnchantedPermanentsDuringOtherPlayersStep(gameData, activePlayerId);
    }

    private void untapSelfPermanentsDuringOtherPlayersStep(GameData gameData, UUID activePlayerId) {
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return;

            for (Permanent permanent : playerBattlefield) {
                if (!permanent.isTapped() || !hasSelfCrossPlayerUntap(gameData, permanent, playerId, TurnStep.UNTAP)) {
                    continue;
                }

                tapUntapSupport.untapPermanent(gameData, permanent);
                String logLine = gameData.playerIdToName.get(playerId) + " untaps " + permanent.getCard().getName()
                        + " during another player's untap step.";
                gameLogService.append(gameData, GameLog.text(logLine));
                log.info("Game {} - {} untaps self-scoped permanent {} during another player's untap step",
                        gameData.id, playerId, permanent.getCard().getName());
            }
        });
    }

    private boolean hasSelfCrossPlayerUntap(GameData gameData, Permanent source, UUID controllerId, TurnStep step) {
        return source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> isActiveSelfCrossPlayerUntap(gameData, source, controllerId, step, effect));
    }

    private boolean isActiveSelfCrossPlayerUntap(GameData gameData, Permanent source, UUID controllerId,
                                                 TurnStep step, CardEffect effect) {
        if (effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect
                && configuredEffect.step() == step
                && configuredEffect.scope() == TapUntapScope.SELF) {
            return true;
        }
        return effect instanceof ConditionalEffect conditional
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStaticEffect(source, controllerId))
                && isActiveSelfCrossPlayerUntap(gameData, source, controllerId, step, conditional.wrapped());
    }

    /** Queues the batched untap-step triggers after all untap choices are complete. */
    public void finishUntapStep(GameData gameData, UUID activePlayerId) {
        if (activePlayerId.equals(gameData.untapStepPlayerId)) {
            triggerCollectionService.checkControllerUntapsDuringUntapStepTriggers(
                    gameData, activePlayerId, gameData.untapStepUntappedPermanentCount);
        }
        gameData.untapStepPlayerId = null;
        gameData.untapStepUntappedPermanentCount = 0;
    }

    /**
     * Untaps every permanent a non-active player controls that is enchanted by an aura granting
     * "Untap this permanent during each other player's untap step" (Urban Burgeoning). The grant
     * lives on the enchanted permanent, so it is the host's controller — not the aura's — that
     * decides whether this untap step counts as "each other player's".
     */
    private void untapEnchantedPermanentsDuringOtherPlayersStep(GameData gameData, UUID activePlayerId) {
        gameData.forEachBattlefield((playerId, playerBattlefield) -> {
            if (playerId.equals(activePlayerId)) return;

            for (Permanent p : playerBattlefield) {
                if (!p.isTapped() || !hasEnchantedCrossPlayerUntap(gameData, p)) continue;

                tapUntapSupport.untapPermanent(gameData, p);
                String logLine = gameData.playerIdToName.get(playerId) + " untaps " + p.getCard().getName()
                        + " during another player's untap step.";
                gameLogService.append(gameData, GameLog.text(logLine));
                log.info("Game {} - {} untaps enchanted permanent {} during another player's untap step",
                        gameData.id, playerId, p.getCard().getName());
            }
        });
    }

    private boolean hasEnchantedCrossPlayerUntap(GameData gameData, Permanent host) {
        return gameData.anyPermanentMatches(aura -> aura.isAttached()
                && aura.getAttachedTo().equals(host.getId())
                && aura.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect untap
                                && untap.scope() == TapUntapScope.ENCHANTED
                                && untap.step() == TurnStep.UNTAP));
    }

    /**
     * Returns {@code true} if any permanent (any controller) carries a
     * {@link PlayersSkipUntapStepEffect}. While true, each player's untap step is skipped entirely
     * (no phasing, no untapping) — see {@link #untapPermanents} with {@code skipUntapStep=true}.
     */
    public boolean playersSkipUntapStepApplies(GameData gameData) {
        return gameData.anyPermanentMatches(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof PlayersSkipUntapStepEffect));
    }

    /**
     * Returns {@code true} if a Storage Matrix untap restriction is in force for the given active
     * player: some untapped permanent (any controller) carries a {@link StorageMatrixEffect} and
     * the active player has at least one tapped permanent to decide about. When there is nothing
     * tapped, the choice would have no observable effect and is skipped.
     */
    public boolean storageMatrixRestrictionApplies(GameData gameData, UUID activePlayerId) {
        boolean untappedMatrixPresent = gameData.anyPermanentMatches(p -> !p.isTapped()
                && p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof StorageMatrixEffect));
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
        List<StaticOrbEffect> active = new ArrayList<>();
        gameData.forEachPermanent((controllerId, p) -> {
            for (CardEffect e : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (e instanceof StaticOrbEffect orb
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
        for (StaticOrbEffect effect : active) {
            if (staticOrbUntapCandidates(gameData, activePlayerId, effect).size() > effect.maxUntap()) {
                return java.util.Optional.of(effect);
            }
        }
        return java.util.Optional.empty();
    }

    private boolean appliesToUntapStep(StaticOrbEffect effect, UUID activePlayerId, UUID sourceControllerId) {
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
     * "doesn't untap", untap locks, a pending skip, a global "doesn't untap" lock, or a "may not
     * untap" choice), or that the filter excludes, never count against the cap and are omitted.
     */
    public List<UUID> staticOrbUntapCandidates(GameData gameData, UUID activePlayerId, StaticOrbEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null) {
            return List.of();
        }
        List<UUID> candidates = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (!p.isTapped() || p.getSkipUntapCount() > 0) {
                continue;
            }
            if (effect.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.filter())) {
                continue;
            }
            boolean hasAttachedDoesntUntap = gameQueryService.hasAuraWithEffect(gameData, p, DoesntUntapEffect.class);
            boolean hasSelfDoesntUntap = untapPreventionSupport.hasActiveSelfDoesntUntap(gameData, p);
            boolean hasMayNotUntap = p.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof MayNotUntapDuringUntapStepEffect);
            boolean hasUntapLock = !p.getUntapPreventedByPermanentIds().isEmpty()
                    || !p.getUntapPreventedWhileSourceOnBattlefieldIds().isEmpty();
            boolean hasMatchingDoesntUntap = matchingStaticPreventsUntap(gameData, p);
            boolean hasParalyzationLock = p.getCounterCount(CounterType.PARALYZATION) > 0;
            boolean hasCounterLock = counterLockPreventsUntap(gameData, p);
            boolean cannotBecomeUntapped = gameQueryService.cantBecomeUntapped(gameData, p);
            if (!hasAttachedDoesntUntap && !hasSelfDoesntUntap && !hasMayNotUntap
                    && !hasUntapLock && !hasMatchingDoesntUntap && !hasParalyzationLock
                    && !hasCounterLock && !cannotBecomeUntapped) {
                candidates.add(p.getId());
            }
        }
        return candidates;
    }

    /**
     * Applies any {@link RemoveCountersInsteadOfUntappingEffect} in force (Freyalise's Winds) to a
     * tapped permanent that is about to untap during its controller's untap step: all counters of
     * the named type are removed from it and it stays tapped.
     *
     * @return {@code true} if the untap was replaced, {@code false} if the permanent should untap
     */
    private boolean removeCountersInsteadOfUntapping(GameData gameData, Permanent permanent) {
        if (!permanent.isTapped()) {
            return false;
        }
        List<CounterType> replaced = new ArrayList<>();
        gameData.forEachPermanent((pid, source) -> {
            for (CardEffect e : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (e instanceof RemoveCountersInsteadOfUntappingEffect replacement
                        && permanent.getCounterCount(replacement.counterType()) > 0) {
                    replaced.add(replacement.counterType());
                }
            }
        });
        if (replaced.isEmpty()) {
            return false;
        }
        for (CounterType counterType : replaced) {
            int removed = permanent.getCounterCount(counterType);
            permanent.setCounterCount(counterType, 0);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, removed);
            }
        }
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                " doesn't untap; its counters are removed instead."));
        log.info("Game {} - {} does not untap; counters {} removed instead",
                gameData.id, permanent.getCard().getName(), replaced);
        return true;
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
        return hasActiveCounterLock(permanent, permanent,
                permanent.getCard().getEffects(EffectSlot.STATIC), TapUntapScope.SELF)
                // Granted, not printed: Mindbender Spores gives the creature it blocks
                // "doesn't untap during your untap step if it has a fungus counter on it".
                || hasActiveCounterLock(permanent, permanent,
                        permanent.getPersistentTriggeredEffects(EffectSlot.STATIC), TapUntapScope.SELF)
                || gameData.anyPermanentMatches(source -> source.isAttached()
                        && source.getAttachedTo().equals(permanent.getId())
                        && (hasActiveCounterLock(source, permanent,
                                source.getCard().getEffects(EffectSlot.STATIC), TapUntapScope.ENCHANTED)
                        || hasActiveCounterLock(source, permanent,
                                source.getPersistentTriggeredEffects(EffectSlot.STATIC), TapUntapScope.ENCHANTED)));
    }

    private static boolean hasActiveCounterLock(Permanent source, Permanent attached,
                                                List<CardEffect> effects, TapUntapScope scope) {
        return effects.stream()
                .anyMatch(e -> e instanceof DoesntUntapWithCounterEffect lock
                        && lock.scope() == scope
                        && counterBearer(source, attached, lock).getCounterCount(lock.counterType()) > 0);
    }

    private static Permanent counterBearer(Permanent source, Permanent attached,
                                           DoesntUntapWithCounterEffect lock) {
        return lock.counterBearer() == PermanentReference.ATTACHED ? attached : source;
    }

    private boolean matchingStaticPreventsUntap(GameData gameData, Permanent permanent) {
        return gameData.anyPermanentMatches(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof MatchingPermanentsDoesntUntapEffect lock
                        // Source-relative filters (An-Zerrin Ruins' chosen creature type) need the
                        // locking permanent as the filter's source, not just the game state.
                        && predicateEvaluationService.matchesPermanentPredicate(permanent, lock.filter(),
                                FilterContext.of(gameData).withSourceCardId(source.getCard().getId()))));
    }

    private List<CrossPlayerUntap> collectUntapOnEachOtherPlayersStepEffects(
            GameData gameData, UUID playerId, TurnStep step) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        List<CrossPlayerUntap> result = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    collectActiveCrossPlayerUntapEffects(gameData, permanent, playerId, step, effect, result);
                }
            }
        }
        for (Emblem emblem : gameData.emblems) {
            if (!playerId.equals(emblem.controllerId())) {
                continue;
            }
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect
                        && configuredEffect.step() == step
                        && configuredEffect.scope() == TapUntapScope.CONTROLLED) {
                    result.add(new CrossPlayerUntap(new Permanent(emblem.sourceCard()), configuredEffect));
                }
            }
        }
        return result;
    }

    private void collectActiveCrossPlayerUntapEffects(
            GameData gameData,
            Permanent source,
            UUID controllerId,
            TurnStep step,
            CardEffect effect,
            List<CrossPlayerUntap> result) {
        if (effect instanceof UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect configuredEffect
                && configuredEffect.step() == step
                && configuredEffect.scope() == TapUntapScope.CONTROLLED) {
            result.add(new CrossPlayerUntap(source, configuredEffect));
            return;
        }
        if (effect instanceof ConditionalEffect conditional
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                ConditionContext.forStaticEffect(source, controllerId))) {
            collectActiveCrossPlayerUntapEffects(
                    gameData, source, controllerId, step, conditional.wrapped(), result);
        }
    }

    private record CrossPlayerUntap(
            Permanent source,
            UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect effect) {
    }

    /**
     * Returns every permanent the active player controls that carries a "return this to its owner's
     * hand during your next untap step" rider (Undiscovered Paradise). Happens as that player
     * untaps their permanents — including when the permanent itself did not untap.
     */
    private void returnPermanentsFlaggedForUntap(GameData gameData, UUID activePlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield == null || battlefield.isEmpty()) {
            return;
        }
        List<Permanent> returning = battlefield.stream()
                .filter(Permanent::isReturnToHandAtNextUntap)
                .toList();
        if (returning.isEmpty()) {
            return;
        }
        for (Permanent permanent : returning) {
            permanent.setReturnToHandAtNextUntap(false);
            permanentRemovalService.removePermanentToHand(gameData, permanent);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
