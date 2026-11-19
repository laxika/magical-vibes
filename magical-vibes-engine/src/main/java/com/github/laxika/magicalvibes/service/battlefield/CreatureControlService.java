package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.RingState;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatAttackRequirementEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CounterConditionedControlEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetHauntedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GoadStatusEffect;
import com.github.laxika.magicalvibes.model.effect.MonarchBoundControlEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentLockEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.UnattachTriggerSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.model.rules.ControlChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * CR 613.2/613.7 layer-2 control semantics. Every control-changing effect is a floating
 * {@code L2_CONTROL} continuous effect on {@link GameData#floatingEffects}; the controller of a
 * permanent is DERIVED — the newest active control effect wins, with none active the permanent
 * reverts to its default controller ({@link GameData#defaultControllerOf}). Because the rest of
 * the engine equates battlefield-list membership with control, {@link #recomputeControl}
 * physically moves the permanent between {@code playerBattlefields} lists whenever the derived
 * controller changes. Moves keep the permanent's CR 613.7 timestamp (a control change is not a
 * zone change — no ETB triggers fire) and make it summoning sick for the new controller
 * (CR 302.6; effects like Threaten grant haste separately).
 */
@Slf4j
@Component
public class CreatureControlService {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final UnattachTriggerSupport unattachTriggerSupport;

    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;

    @Autowired
    @Lazy
    private DurationClock durationClock;

    @Autowired
    @Lazy
    private CreaturePositioningSupport creaturePositioningSupport;

    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.AscendEffectHandler ascendEffectHandler;

    @Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.effect.normalfx.StoriedEffectHandler storiedEffectHandler;

    @Autowired
    public CreatureControlService(GameLogService gameLogService, GameQueryService gameQueryService,
                                  UnattachTriggerSupport unattachTriggerSupport) {
        this.gameLogService = gameLogService;
        this.gameQueryService = gameQueryService;
        this.unattachTriggerSupport = unattachTriggerSupport;
    }

    public CreatureControlService(GameLogService gameLogService, GameQueryService gameQueryService) {
        this(gameLogService, gameQueryService, new UnattachTriggerSupport());
    }

    /**
     * Creates a floating control effect giving {@code newControllerId} control of {@code target}
     * and immediately recomputes the target's controller.
     *
     * @param gameData          the current game state
     * @param newControllerId   the player the effect gives control to
     * @param target            the permanent changing control
     * @param wrappedEffect     the control effect being applied (drives layer classification)
     * @param duration          how long the effect lasts
     * @param sourcePermanentId the source permanent for source/attachment-scoped durations, else {@code null}
     * @param sourceCardName    name of the card whose spell/ability created the effect
     */
    public boolean applyControlEffect(GameData gameData, UUID newControllerId, Permanent target,
                                   CardEffect wrappedEffect, Duration duration,
                                   UUID sourcePermanentId, String sourceCardName) {
        return applyControlEffect(gameData, newControllerId, target, wrappedEffect, duration,
                sourcePermanentId, sourceCardName, null);
    }

    /** Applies control with an optional player id whose monarch status governs the effect's lifetime. */
    public boolean applyControlEffect(GameData gameData, UUID newControllerId, Permanent target,
                                   CardEffect wrappedEffect, Duration duration,
                                   UUID sourcePermanentId, String sourceCardName,
                                   UUID affectedPlayerId) {
        UUID currentControllerId = gameData.findControllerOf(target);
        if (currentControllerId != null && !currentControllerId.equals(newControllerId)
                && gameQueryService.cantBeControlledByOtherPlayers(gameData, target)) {
            return false;
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceCardName, sourcePermanentId, newControllerId,
                wrappedEffect, target.getId(), affectedPlayerId, null, duration, 0));
        recomputeControl(gameData, target);
        return true;
    }

    /**
     * Gives {@code newControllerId} control of each of the given permanents as one simultaneous
     * event: every control effect is stamped before any permanent moves, then control is recomputed
     * for all of them together (see {@link #recomputeControls}). Permanents that cannot be controlled
     * by other players are skipped.
     */
    public void applyControlEffectSimultaneously(GameData gameData, UUID newControllerId,
                                                 List<Permanent> targets, CardEffect wrappedEffect,
                                                 Duration duration, String sourceCardName) {
        List<Permanent> stamped = new ArrayList<>();
        for (Permanent target : targets) {
            UUID currentControllerId = gameData.findControllerOf(target);
            if (currentControllerId != null && !currentControllerId.equals(newControllerId)
                    && gameQueryService.cantBeControlledByOtherPlayers(gameData, target)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), sourceCardName, null, newControllerId,
                    wrappedEffect, target.getId(), null, null, duration, 0));
            stamped.add(target);
        }
        recomputeControls(gameData, stamped);
    }

    /**
     * Exchanges control of two permanents as one simultaneous event (CR 701.12a). The caller has
     * already checked that both permanents may change controllers; both control effects are stamped
     * before either is recomputed, so a "can't be controlled by other players" ability that the first
     * permanent carries (Guardian Beast) can't block the second half once it has switched sides.
     */
    public void exchangeControl(GameData gameData, Permanent first, UUID firstNewControllerId,
                                Permanent second, UUID secondNewControllerId,
                                CardEffect wrappedEffect, Duration duration, String sourceCardName) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceCardName, null, firstNewControllerId,
                wrappedEffect, first.getId(), null, null, duration, 0));
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceCardName, null, secondNewControllerId,
                wrappedEffect, second.getId(), null, null, duration, 0));
        recomputeControl(gameData, first);
        recomputeControl(gameData, second);
    }

    /**
     * Applies the control changes queued by a spent Debt of Loyalty regeneration shield
     * ({@link GameData#pendingRegenerationControlChanges}). Regeneration is resolved inside the
     * state-based-action sweep, which iterates the battlefield lists this moves permanents between,
     * so the change is queued there and flushed here once the sweep is done.
     *
     * @return true when at least one control change was applied
     */
    public boolean applyPendingRegenerationControlChanges(GameData gameData) {
        if (gameData.pendingRegenerationControlChanges.isEmpty()) {
            return false;
        }
        List<UUID> permanentIds = new ArrayList<>(gameData.pendingRegenerationControlChanges.keySet());
        boolean applied = false;
        for (UUID permanentId : permanentIds) {
            UUID newControllerId = gameData.pendingRegenerationControlChanges.remove(permanentId);
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || newControllerId == null) {
                continue;
            }
            boolean controlApplied = applyControlEffect(gameData, newControllerId, permanent,
                    new GainControlOfTargetEffect(Duration.PERMANENT), Duration.PERMANENT,
                    null, "Debt of Loyalty");
            applied |= controlApplied;
        }
        return applied;
    }

    /**
     * Hook for the tap/untap machinery: when a permanent becomes untapped, the effects that last for
     * as long as it remains tapped end (CR 611.2b), and a permanent one of them held (Seasinger)
     * reverts to its controller immediately. No-op when the untapped permanent is the source of no
     * such effect, so it is cheap to call on every untap.
     */
    public void onSourceUntapped(GameData gameData, Permanent permanent) {
        if (durationClock.sourceUntapped(gameData, permanent.getId()).stream()
                .anyMatch(FloatingContinuousEffect::isControlEffect)) {
            reconcileControl(gameData);
        }
    }

    /**
     * Recomputes who controls the permanent from the active control effects and physically
     * moves it between battlefield lists if the derived controller differs from the current
     * one. Maintains the {@link GameData#stolenCreatures} ownership record (first move away
     * from the owner records them; arriving back at the owner clears the record).
     *
     * <p>No-ops if the permanent is not on any battlefield or control is unchanged.
     */
    public void recomputeControl(GameData gameData, Permanent permanent) {
        recomputeControls(gameData, List.of(permanent));
    }

    /**
     * Recomputes control of several permanents as one simultaneous event. Control-change triggers
     * are collected for every changing permanent while all of them still sit on their previous
     * controller's battlefield, so a permanent that is itself changing control still sees the other
     * changes (Insurrection stealing Zidane and another creature at once makes Zidane trigger twice).
     */
    public void recomputeControls(GameData gameData, Collection<Permanent> permanents) {
        List<ControlChange> changes = new ArrayList<>();
        for (Permanent permanent : permanents) {
            UUID current = gameData.findControllerOf(permanent);
            if (current == null) {
                continue;
            }
            UUID derived = gameData.deriveControllerOf(permanent.getId());
            if (derived == null || derived.equals(current)) {
                continue;
            }
            changes.add(new ControlChange(permanent, current, derived));
        }
        for (ControlChange change : changes) {
            gameData.rulesEvents.occur(new ControlChangeEvent.OpponentGains(change.permanent(),
                    change.previousControllerId(), change.newControllerId()));
        }
        for (ControlChange change : changes) {
            moveControl(gameData, change.permanent(), change.previousControllerId(), change.newControllerId());
        }
    }

    private record ControlChange(Permanent permanent, UUID previousControllerId, UUID newControllerId) {
    }

    private void moveControl(GameData gameData, Permanent permanent, UUID current, UUID derived) {
        gameData.ringStates.replaceAll((playerId, ringState) ->
                permanent.getId().equals(ringState.bearerId())
                        ? new RingState(ringState.level(), null)
                        : ringState);
        queueSelfControlLossTriggers(gameData, permanent, current);
        boolean revertedToDefault = gameData.newestControlEffectFor(permanent.getId()) == null;

        removeFromCombat(gameData, permanent);
        gameData.playerBattlefields.get(current).remove(permanent);
        gameData.playerBattlefields.get(derived).add(permanent);
        permanent.recordControlChange();
        permanent.setSummoningSick(true);
        if (creaturePositioningSupport != null) {
            creaturePositioningSupport.onControlChange(gameData, derived, permanent);
        }
        boolean retainsFaceDownExileAccess = permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                .flatMap(ability -> ability.getEffects().stream())
                .anyMatch(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect
                        .ExileCardFromHandFaceDownWithSourceEffect exile && !exile.toGraveyardOnControlLoss());
        retainsFaceDownExileAccess |= java.util.stream.Stream.concat(
                        java.util.stream.Stream.concat(permanent.getCard().getCastingCostsAndSpellEffects().stream(),
                                permanent.getCard().getStaticEffects().stream()),
                        permanent.getCard().getTriggeredEffects().stream())
                .anyMatch(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect
                        .ExileTopCardsToSourceEffect exile && exile.faceDown()
                        && exile.mayLookAtFaceDownCards() && !exile.toGraveyardOnControlLoss());
        if (!permanent.isFaceDown() && !gameQueryService.hasLostAllAbilities(gameData, permanent)
                && retainsFaceDownExileAccess) {
            for (var exiled : gameData.getExiledWithPermanentEntries(permanent.getId(), permanent.getCard().getId())) {
                if (!exiled.faceDown()) continue;
                gameData.grantExileLookPermission(exiled.card().getId(), current);
                gameData.grantExileLookPermission(exiled.card().getId(), derived);
            }
        }
        if (ascendEffectHandler != null) {
            ascendEffectHandler.checkPermanentAscend(gameData, derived);
        }
        if (storiedEffectHandler != null) {
            storiedEffectHandler.checkPermanentStoried(gameData, derived);
        }
        // Soulbond lasts only while you control both (CR 702.95) — control change breaks the pair.
        UUID partnerId = permanent.getPairedWithId();
        if (partnerId != null) {
            permanent.setPairedWithId(null);
            Permanent partner = gameQueryService.findPermanentById(gameData, partnerId);
            if (partner != null && permanent.getId().equals(partner.getPairedWithId())) {
                partner.setPairedWithId(null);
            }
        }

        UUID ownerId = permanent.getOriginalCard().getOwnerId();
        gameData.stolenCreatures.putIfAbsent(permanent.getId(), ownerId != null ? ownerId : current);
        if (derived.equals(gameData.stolenCreatures.get(permanent.getId()))) {
            gameData.stolenCreatures.remove(permanent.getId());
        }

        String newControllerName = gameData.playerIdToName.get(derived);
        if (revertedToDefault) {
            gameLogService.append(gameData, GameLog.builder()
                    .card(permanent.getCard())
                    .text(" returns to " + newControllerName + "'s control.")
                    .build());
        } else {
            gameLogService.append(gameData, GameLog.textCardText(
                    newControllerName + " gains control of ", permanent.getCard(), "."));
        }
        log.info("Game {} - {} controls {}", gameData.id, newControllerName, permanent.getCard().getName());

        queueSelfControlChangeTriggers(gameData, permanent, derived);

        // "For as long as you control [source]" effects keyed to THIS permanent end when it
        // changes controllers away from their creator (CR 611.2b); cascade to the permanents they held.
        for (FloatingContinuousEffect ended : durationClock.sourceChangedControl(gameData, permanent, derived)) {
            Permanent affected = gameQueryService.findPermanentById(gameData, ended.affectedPermanentId());
            if (affected != null) {
                recomputeControl(gameData, affected);
            }
        }
    }

    private void queueSelfControlChangeTriggers(GameData gameData, Permanent permanent, UUID controllerId) {
        gameData.rulesEvents.occur(new ControlChangeEvent.Gained(permanent, controllerId));
    }

    private void queueSelfControlLossTriggers(GameData gameData, Permanent permanent,
                                              UUID controllerId) {
        gameData.rulesEvents.occur(new ControlChangeEvent.Lost(permanent, controllerId));
    }

    private void removeFromCombat(GameData gameData, Permanent permanent) {
        List<UUID> blockedAttackerIds = new ArrayList<>(permanent.getBlockingTargetIds());
        permanent.clearCombatState();

        for (UUID attackerId : blockedAttackerIds) {
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            if (attacker != null && attacker.isAttacking()
                    && !gameQueryService.isBlockedByAnyCreature(gameData, attacker)) {
                attacker.setBlockedWithoutBlockers(true);
            }
        }
    }

    /**
     * Full control reconciliation: ensures every attached control Aura (In Bolas's Clutches,
     * Persuasion, ...) is backed by a {@code WHILE_ATTACHED} floating effect, expires effects
     * whose condition no longer holds, and recomputes control of every permanent touched by a
     * control effect or ownership record. Called after battlefield removals
     * ({@code AuraAttachmentService.removeOrphanedAuras}) and at the cleanup step, replacing
     * the old {@code returnStolenCreatures} revert-to-owner scan — control falls back to the
     * NEXT most recent still-active effect, not blindly to the owner.
     */
    public void reconcileControl(GameData gameData) {
        ensureControlAuraEffects(gameData);
        expireStaleControlEffects(gameData);

        Set<UUID> affectedIds = new LinkedHashSet<>(gameData.stolenCreatures.keySet());
        for (FloatingContinuousEffect fe : List.copyOf(gameData.floatingEffects)) {
            if (fe.isControlEffect() && fe.affectedPermanentId() != null) {
                affectedIds.add(fe.affectedPermanentId());
            }
        }
        for (UUID permanentId : affectedIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null && isPhasedOut(gameData, permanentId)) {
                // Still in the game: its control is settled when it phases back in (PhasingService).
                continue;
            }
            if (permanent == null) {
                gameData.stolenCreatures.remove(permanentId);
                gameData.expireControlEffectsForDepartedPermanent(permanentId);
                continue;
            }
            recomputeControl(gameData, permanent);
        }
    }

    private boolean isPhasedOut(GameData gameData, UUID permanentId) {
        return gameData.phasedOutPermanents.values().stream()
                .anyMatch(list -> list.stream().anyMatch(p -> p.getId().equals(permanentId)));
    }

    /**
     * Every Aura with a {@link ControlEnchantedCreatureEffect} static ability that is attached
     * to a permanent must have a matching {@code WHILE_ATTACHED} floating control effect. The
     * resolution sites that attach control Auras create it directly (so the steal is immediate);
     * this safety net covers reattachment paths (Aura Graft, ...) where the old attachment's
     * effect was expired and a new one is due at the new attach timestamp.
     */
    private void ensureControlAuraEffects(GameData gameData) {
        List<Permanent> controlAuras = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.isAttached()
                    && p.getCard().getStaticEffects().stream()
                            .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect)) {
                controlAuras.add(p);
            }
        });
        for (Permanent aura : controlAuras) {
            Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
            if (enchanted == null) continue;
            CardEffect controlEffect = aura.getCard().getStaticEffects().stream()
                    .filter(e -> e instanceof ControlEnchantedCreatureEffect)
                    .findFirst()
                    .orElseThrow();
            boolean monarchBound = ((ControlEnchantedCreatureEffect) controlEffect).controlledByMonarch();
            UUID controllingPlayerId = monarchBound
                    ? gameData.monarchPlayerId
                    : gameData.findControllerOf(aura);
            if (monarchBound) {
                gameData.expireFloatingEffects(fe -> Duration.WHILE_ATTACHED.equals(fe.duration())
                        && fe.isControlEffect()
                        && aura.getId().equals(fe.sourcePermanentId())
                        && !Objects.equals(fe.controllerId(), controllingPlayerId));
            }
            boolean present = gameData.controlEffectsFor(enchanted.getId()).stream()
                    .anyMatch(fe -> Duration.WHILE_ATTACHED.equals(fe.duration())
                            && aura.getId().equals(fe.sourcePermanentId()));
            if (!present && controllingPlayerId != null) {
                gameData.putFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), aura.getCard().getName(), aura.getId(), controllingPlayerId,
                        controlEffect, enchanted.getId(), null, null,
                        Duration.WHILE_ATTACHED, aura.getTimestamp()));
            }
        }
    }

    /**
     * Ends the source-controller-dependent floating effects whose condition stopped holding; an ended
     * effect doesn't resume if the condition holds again:
     * <ul>
     *   <li>a "for as long as" duration that no longer holds ({@link DurationClock#holds}): the
     *       state changes end these through the clock already, and this catches a change made
     *       without telling it.</li>
     *   <li>{@link GainControlOfTargetEffect} that maintains its target's predicate (Old Man of the
     *       Sea), a counter-conditioned control effect, {@link GainControlOfEnchantedTargetEffect}
     *       (Rootwater Matriarch), a haunted creature and a monarch-bound control effect.</li>
     * </ul>
     */
    private void expireStaleControlEffects(GameData gameData) {
        for (FloatingContinuousEffect fe : List.copyOf(gameData.floatingEffects)) {
            boolean sourceControllerDependent = fe.isControlEffect()
                    || Duration.WHILE_SOURCE_CONTROLLED.equals(fe.duration())
                    || (fe.effect() instanceof CombatAttackRequirementEffect requirement
                    && requirement.endsWhenSourceControllerChanges())
                    || fe.effect() instanceof GoadStatusEffect
                    || (fe.effect() instanceof PermanentLockEffect lock
                    && lock.endsWhenSourceControllerChanges());
            if (!sourceControllerDependent) continue;
            boolean stale = !durationClock.holds(gameData, fe);
            if (!stale && fe.effect() instanceof GainControlOfTargetEffect control
                    && control.maintainTargetPredicate()) {
                Permanent source = fe.sourcePermanentId() == null ? null
                        : gameQueryService.findPermanentById(gameData, fe.sourcePermanentId());
                Permanent affected = gameQueryService.findPermanentById(gameData, fe.affectedPermanentId());
                FilterContext context = source == null ? null : new FilterContext(
                        gameData, source.getCard().getId(), fe.controllerId(), null, source, source.getId());
                // A permanent that stops being a creature has power 0, so Old Man of the Sea's
                // "power remains less than or equal" condition still holds and control continues.
                stale = affected == null || source == null || predicateEvaluationService == null
                        || (gameQueryService.isCreature(gameData, affected)
                        && !predicateEvaluationService.matchesPermanentPredicate(
                        affected, control.targetPredicate(), context));
            }
            if (!stale && fe.effect() instanceof CounterConditionedControlEffect counterConditioned) {
                Permanent affected = gameQueryService.findPermanentById(gameData, fe.affectedPermanentId());
                stale = affected == null
                        || affected.getCounterCount(counterConditioned.counterType()) <= 0;
            }
            if (!stale && fe.effect() instanceof GainControlOfEnchantedTargetEffect) {
                Permanent affected = gameQueryService.findPermanentById(gameData, fe.affectedPermanentId());
                stale = affected == null || !gameQueryService.isEnchanted(gameData, affected);
            }
            if (!stale && fe.effect() instanceof GainControlOfTargetHauntedCreatureEffect) {
                stale = !gameData.hauntingCardToPermanentId.containsValue(fe.affectedPermanentId());
            }
            if (!stale && fe.effect() instanceof MonarchBoundControlEffect) {
                stale = !Objects.equals(gameData.monarchPlayerId, fe.affectedPlayerId());
            }
            if (stale) {
                gameData.endFloatingEffect(fe);
            }
        }
    }
}
