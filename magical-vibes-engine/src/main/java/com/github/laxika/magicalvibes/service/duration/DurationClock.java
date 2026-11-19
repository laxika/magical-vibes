package com.github.laxika.magicalvibes.service.duration;

import com.github.laxika.magicalvibes.model.rules.PermanentChange;
import com.github.laxika.magicalvibes.model.rules.PermanentChangeEvent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BombardmentEffect;
import com.github.laxika.magicalvibes.model.effect.ResolvedCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatAttackRequirementEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GoadStatusEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.NextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentLockEffect;
import com.github.laxika.magicalvibes.model.effect.PhasedOutHeldEffect;
import com.github.laxika.magicalvibes.model.effect.TapRestrictionEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.rules.TapEvent;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The clock that ends the durations of the continuous effects resolving spells and abilities made (CR 611.2a). The
 * trigger pipeline hands it each rules event batch before it matches the batch's triggered abilities; the clock ends
 * every floating effect whose {@link Duration.End} is one of the batch's events, without the stack (as CR 514.2), and
 * undoes what the effect kept on the permanent it affected (a permanent a phasing hold kept out phases in).
 *
 * <p>The events that end durations: a turn beginning ({@link TurnEvent.Began}: "until your next turn", and control
 * of a player through their whole turn, CR 724.1), a step or phase ending ({@link TurnEvent.StepEnded}: "until end
 * of combat", "until the end of your next upkeep", CR 500.5), the cleanup step's damage removal ({@link TurnEvent.Cleanup}: "until end of turn", "until the end of your next turn",
 * CR 514.2), a permanent being turned face up ("for as long as it remains face down"), a step beginning
 * ({@link StepEvent}: "until your next upkeep", "until the next end step"), the untap step's
 * untapping ("until its controller's next untap step"), a spell being cast ({@link SpellEvent.Cast}) and a planeswalk
 * ({@link ActionEvent.Planeswalked}). A turn beginning also begins the effects scheduled to begin with it ("during your
 * next turn", "after this turn"), and drops the ones that were to begin at a combat phase of the turn that ended; the
 * beginning of combat begins the ones scheduled for the next combat of their affected permanent's controller, and the
 * ones scheduled for that combat phase of the turn ("during that combat", see {@link Duration#start()}).
 *
 * <p>A "for as long as" duration (CR 611.2b) ends as soon as its condition stops holding, not at a batch: the
 * engine tells the clock where the state changes, and the clock ends the effects of that source the change ends. Its
 * source leaves the battlefield ({@link #sourceLeft}), phases out ({@link #trackedPermanentPhasedOut}, CR 702.26f),
 * untaps ({@link #sourceUntapped}), becomes unattached or attached to another object ({@link #sourceUnattached}), or
 * comes under the control of a player other than the effect's creator ({@link #sourceChangedControl}). Whatever their
 * durations, the effects on a permanent end as it leaves the battlefield ({@link #affectedLeft}, CR 400.7). {@link #holds} judges the same conditions against the board, for the control
 * reconciliation that catches a change made without telling the clock. Whatever ends an effect, the clock undoes what
 * it kept on the permanent it affected; control itself is recomputed by its caller, which may be iterating a
 * battlefield.
 */
@Service
public class DurationClock {

    private final PredicateEvaluationService predicateEvaluationService;

    private final GameQueryService gameQueryService;

    private final PhasingService phasingService;

    public DurationClock(@Lazy PredicateEvaluationService predicateEvaluationService,
                         @Lazy GameQueryService gameQueryService,
                         @Lazy PhasingService phasingService) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
        this.phasingService = phasingService;
    }

    /** Ends the durations the events of {@code batch} end, in event order. */
    public void advance(GameData gameData, RulesEventBatch batch) {
        for (RulesEvent event : batch.events()) {
            advance(gameData, event);
        }
    }

    /**
     * Ends the floating effects whose duration {@code event} ends, and returns them. A turn beginning also begins the
     * scheduled effects whose duration starts with it, after the effects it ends have ended.
     */
    public List<FloatingContinuousEffect> advance(GameData gameData, RulesEvent event) {
        Predicate<FloatingContinuousEffect> ends = endsAt(gameData, event);
        List<FloatingContinuousEffect> ended = ends == null ? List.of() : end(gameData, ends);
        if (event instanceof TurnEvent.Began began) {
            gameData.dropScheduledFloatingEffects(effect -> effect.duration().start()
                    == Duration.Start.COMBAT_PHASE_OF_THIS_TURN);
            gameData.startScheduledFloatingEffects(effect -> startsAt(gameData, began, effect));
        } else if (event instanceof StepEvent step && step.moment() == StepTrigger.Moment.BEGINNING_OF_COMBAT) {
            gameData.startScheduledFloatingEffects(effect -> startsAtCombat(gameData, step, effect));
        }
        return ended;
    }

    /**
     * Whether the beginning of combat {@code step} begins the scheduled {@code effect}: "during its controller's next
     * combat phase" at a combat of its affected permanent's controller, "during that combat" at the combat phase of
     * this turn it names.
     */
    private boolean startsAtCombat(GameData gameData, StepEvent step, FloatingContinuousEffect effect) {
        return switch (effect.duration().start()) {
            case AFFECTED_CONTROLLERS_NEXT_COMBAT -> effect.affectedPermanentId() != null
                    ? step.activePlayerId().equals(
                    gameQueryService.findPermanentController(gameData, effect.affectedPermanentId()))
                    : step.activePlayerId().equals(effect.affectedPlayerId());
            case COMBAT_PHASE_OF_THIS_TURN -> effect.duration().combatPhase() == gameData.combatPhasesThisTurn;
            default -> false;
        };
    }

    /**
     * Whether the turn {@code began} begins the scheduled {@code effect}: "after this turn" begins at any next turn,
     * "during your next turn" at its controller's, "during target player's next turn" at the affected player's, or at
     * its controller's when the effect affects a permanent ("during its controller's next turn").
     */
    private boolean startsAt(GameData gameData, TurnEvent.Began began, FloatingContinuousEffect effect) {
        UUID activePlayerId = began.activePlayerId();
        return switch (effect.duration().start()) {
            case NOW, AFTER_THIS_TURN -> true;
            case AFFECTED_CONTROLLERS_NEXT_COMBAT, COMBAT_PHASE_OF_THIS_TURN -> false;
            case YOUR_NEXT_TURN -> activePlayerId.equals(effect.controllerId());
            case AFFECTED_PLAYERS_NEXT_TURN -> effect.affectedPermanentId() != null
                    ? activePlayerId.equals(gameQueryService.findPermanentController(gameData,
                    effect.affectedPermanentId()))
                    : activePlayerId.equals(effect.affectedPlayerId());
        };
    }

    /**
     * Ends the "for as long as" effects (CR 611.2b) that hold only while {@code sourcePermanentId} is on the
     * battlefield, as it leaves, and returns them.
     */
    public List<FloatingContinuousEffect> sourceLeft(GameData gameData, UUID sourcePermanentId) {
        return end(gameData, effect -> sourcePermanentId.equals(effect.sourcePermanentId())
                && asLongAs(effect) != null && asLongAs(effect).needsSourceOnBattlefield());
    }

    /**
     * Ends the floating effects on {@code permanentId} as it leaves the battlefield, whatever their durations: the
     * object they affected is gone, and the one that returns is a new object they don't apply to (CR 400.7). They stay
     * readable as the departed permanent's last-known information (CR 608.2h), so call this after its departure was
     * recorded. Its control effects end with its control (see {@code GameData.expireControlEffectsForDepartedPermanent}).
     * Phasing out is not leaving (CR 702.26d).
     */
    public List<FloatingContinuousEffect> affectedLeft(GameData gameData, UUID permanentId) {
        return gameData.endFloatingEffectsOnDepartedPermanent(permanentId);
    }

    /**
     * Ends the "for as long as" effects that track {@code permanentId} as it phases out, because they can no longer see
     * it (CR 702.26f): the ones whose duration follows it as their source ("for as long as you control this creature",
     * "for as long as this creature remains tapped", the permanent itself included), and the ones that last for as long
     * as it, the permanent they affect, remains face down or keeps an Equipment attached. They don't begin again as it
     * phases in. "Until it leaves the battlefield" and "until you lose control of it" go on, as phasing out is neither
     * (CR 702.26d), and so do the effects with other durations ("until end of turn", "until end of combat") and an
     * attachment a rule or a static ability keeps.
     */
    public List<FloatingContinuousEffect> trackedPermanentPhasedOut(GameData gameData, UUID permanentId) {
        return end(gameData, effect -> {
            Duration.AsLongAs asLongAs = asLongAs(effect);
            if (asLongAs == null) {
                return false;
            }
            if (asLongAs.endsAsSourcePhasesOut() && permanentId.equals(effect.sourcePermanentId())) {
                return true;
            }
            return (asLongAs == Duration.AsLongAs.AFFECTED_FACE_DOWN || asLongAs == Duration.AsLongAs.REMAINS_ATTACHED)
                    && permanentId.equals(effect.affectedPermanentId());
        });
    }

    /** Ends the effects that last for as long as {@code sourcePermanentId} remains tapped, as it untaps. */
    public List<FloatingContinuousEffect> sourceUntapped(GameData gameData, UUID sourcePermanentId) {
        return end(gameData, effect -> sourcePermanentId.equals(effect.sourcePermanentId())
                && asLongAs(effect) != null && asLongAs(effect).needsSourceTapped());
    }

    /**
     * Ends the effects that last for as long as the Aura or Equipment {@code sourcePermanentId} stays attached, as it
     * becomes unattached or attached to another object.
     */
    public List<FloatingContinuousEffect> sourceUnattached(GameData gameData, UUID sourcePermanentId) {
        return end(gameData, effect -> sourcePermanentId.equals(effect.sourcePermanentId())
                && asLongAs(effect) != null && asLongAs(effect).needsAttachment());
    }

    /**
     * Ends the effects that last only while their creator controls {@code source}, as it comes under
     * {@code controllerId}'s control and their creator is another player.
     */
    public List<FloatingContinuousEffect> sourceChangedControl(GameData gameData, Permanent source,
                                                               UUID controllerId) {
        return end(gameData, effect -> source.getId().equals(effect.sourcePermanentId())
                && needsCreatorToControlSource(effect) && !effect.controllerId().equals(controllerId));
    }

    /**
     * Whether the "for as long as" condition of {@code effect} still holds on the board (CR 611.2b): its source is on
     * the battlefield, under its creator's control when the effect needs that, tapped when it needs that, and
     * attached to the affected permanent when it lasts while attached. A duration that ends at an event holds.
     */
    public boolean holds(GameData gameData, FloatingContinuousEffect effect) {
        Duration.AsLongAs asLongAs = asLongAs(effect);
        if (asLongAs == null || !asLongAs.needsSourceOnBattlefield()) {
            return true;
        }
        Permanent source = effect.sourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, effect.sourcePermanentId());
        if (source == null) {
            return false;
        }
        if (needsCreatorToControlSource(effect) && !effect.controllerId().equals(gameData.findControllerOf(source))) {
            return false;
        }
        if (asLongAs.needsSourceTapped() && !source.isTapped()) {
            return false;
        }
        return !asLongAs.needsAttachment()
                || (source.isAttached() && source.getAttachedTo().equals(effect.affectedPermanentId()));
    }

    /**
     * Whether {@code effect} lasts only while its creator controls its source: "for as long as you control this
     * Saga", and a source-linked control effect, goad, attack requirement, lock or grant, which say "for as long as
     * you control" it (Olivia Voldaren, Seasinger).
     */
    private static boolean needsCreatorToControlSource(FloatingContinuousEffect effect) {
        Duration.AsLongAs asLongAs = asLongAs(effect);
        if (asLongAs == Duration.AsLongAs.SOURCE_CONTROLLED || asLongAs == Duration.AsLongAs.SOURCE_UNTIL_CONTROL_LOST) {
            return true;
        }
        if (asLongAs != Duration.AsLongAs.SOURCE_ON_BATTLEFIELD && asLongAs != Duration.AsLongAs.SOURCE_TAPPED) {
            return false;
        }
        CardEffect wrapped = effect.effect();
        return effect.isControlEffect()
                || wrapped instanceof GoadStatusEffect
                || wrapped instanceof GrantKeywordEffect
                || wrapped instanceof GrantActivatedAbilityEffect
                || (wrapped instanceof CombatAttackRequirementEffect requirement
                && requirement.endsWhenSourceControllerChanges())
                || (wrapped instanceof PermanentLockEffect lock && lock.endsWhenSourceControllerChanges());
    }

    private static Duration.AsLongAs asLongAs(FloatingContinuousEffect effect) {
        return effect.duration() == null ? null : effect.duration().asLongAs();
    }

    /** Ends the floating effects {@code ends} matches, undoes what each kept, and returns them. */
    private List<FloatingContinuousEffect> end(GameData gameData, Predicate<FloatingContinuousEffect> ends) {
        List<FloatingContinuousEffect> ended = gameData.expireFloatingEffects(ends);
        for (FloatingContinuousEffect effect : ended) {
            undo(gameData, effect);
        }
        endCopies(gameData, ended);
        return ended;
    }

    /** Which floating effects {@code event} ends, or {@code null} when it ends none. */
    private Predicate<FloatingContinuousEffect> endsAt(GameData gameData, RulesEvent event) {
        return switch (event) {
            case TurnEvent.Began began -> effect -> ends(effect, Duration.End.NEXT_TURN)
                    || (ends(effect, Duration.End.YOUR_NEXT_TURN)
                    && began.activePlayerId().equals(effect.controllerId()));
            case TurnEvent.StepEnded ended when ended.step() == TurnStep.END_OF_COMBAT ->
                    effect -> ends(effect, Duration.End.END_OF_COMBAT);
            case TurnEvent.StepEnded ended when ended.step() == TurnStep.UPKEEP ->
                    effect -> ends(effect, Duration.End.END_OF_UPKEEP)
                            || (ended.activePlayerId() != null
                            && ends(effect, Duration.End.END_OF_YOUR_NEXT_UPKEEP)
                            && ended.activePlayerId().equals(effect.controllerId())
                            && startedBefore(effect, gameData.upkeepStartTimestamp));
            case TurnEvent.Cleanup cleanup -> effect -> endsAtCleanup(gameData, cleanup.activePlayerId(), effect);
            case StepEvent step when step.moment() == StepTrigger.Moment.UPKEEP ->
                    effect -> ends(effect, Duration.End.YOUR_NEXT_UPKEEP)
                            && step.activePlayerId().equals(effect.controllerId());
            case StepEvent step when step.moment() == StepTrigger.Moment.END_STEP ->
                    effect -> ends(effect, Duration.End.NEXT_END_STEP)
                            || (ends(effect, Duration.End.YOUR_NEXT_END_STEP)
                            && step.activePlayerId().equals(effect.controllerId()));
            case TapEvent.UntappedDuringUntapStep untapped ->
                    effect -> ends(effect, Duration.End.AFFECTED_CONTROLLERS_NEXT_UNTAP_STEP)
                            && effect.affectedPermanentId() != null
                            && untapped.controllerId().equals(
                            gameQueryService.findPermanentController(gameData, effect.affectedPermanentId()));
            case PermanentChangeEvent change when change.change() == PermanentChange.TURNED_FACE_UP ->
                    effect -> asLongAs(effect) == Duration.AsLongAs.AFFECTED_FACE_DOWN
                            && change.permanent().getId().equals(effect.affectedPermanentId());
            case SpellEvent.Cast cast -> effect -> endsAtCast(gameData, cast, effect);
            case ActionEvent.Planeswalked ignored -> effect -> ends(effect, Duration.End.PLANESWALK);
            default -> null;
        };
    }

    /**
     * Whether the cleanup step in {@code activePlayerId}'s turn ends {@code effect}: an "until end of turn" effect, a
     * reduction of the next matching spell that lasts the turn, an "until the end of your next turn" effect of the
     * active player that began before this turn, and a "this turn and next turn" effect that began before this turn
     * (CR 514.2). While players take their phases one after another
     * (Runed Terror), a turn's effects end only at the cleanup step of the player who was active as they began.
     */
    private boolean endsAtCleanup(GameData gameData, UUID activePlayerId, FloatingContinuousEffect effect) {
        if (ends(effect, Duration.End.END_OF_YOUR_NEXT_TURN)) {
            return activePlayerId != null && activePlayerId.equals(effect.controllerId())
                    && startedBefore(effect, gameData.turnStartTimestamp);
        }
        if (ends(effect, Duration.End.END_OF_NEXT_TURN)) {
            return startedBefore(effect, gameData.turnStartTimestamp);
        }
        boolean endsThisTurn = ends(effect, Duration.End.END_OF_TURN)
                || (ends(effect, Duration.End.MATCHING_SPELL_CAST)
                && !(effect.effect() instanceof NextMatchingSpellEffect next && !next.expiresAtCleanup()));
        if (!endsThisTurn) {
            return false;
        }
        if (!gameData.runedTerrorPhaseSequenceActive || activePlayerId == null || effect.id() == null) {
            return true;
        }
        return activePlayerId.equals(
                gameData.floatingEffectCreatingActivePlayerIds.getOrDefault(effect.id(), activePlayerId));
    }

    /**
     * Whether casting {@code cast}'s spell ends {@code effect}: a grant that lasts until its card is cast from exile,
     * an effect that waits for the next spell it matches (a cost change, a flash, free-cast or life-payment grant, a rider), and an effect that lasts until a creature spell is cast.
     */
    private boolean endsAtCast(GameData gameData, SpellEvent.Cast cast, FloatingContinuousEffect effect) {
        if (ends(effect, Duration.End.SOURCE_CARD_CAST_FROM_EXILE)) {
            return cast.castZone() == Zone.EXILE
                    && effect.effect() instanceof GrantActivatedAbilityEffect grant
                    && cast.spellCard().getId().equals(grant.expirationCardId());
        }
        if (ends(effect, Duration.End.MATCHING_SPELL_CAST)
                || ends(effect, Duration.End.TARGET_PLAYER_MATCHING_SPELL_CAST)) {
            return effect.effect() instanceof NextMatchingSpellEffect next
                    && next.appliesToPlayer(cast.castingPlayerId(), effect.controllerId())
                    && next.appliesToFaceDownCast(cast.castFaceDown())
                    && predicateEvaluationService.matchesCardPredicate(
                    cast.spellCard(), next.predicate(), null, gameData, cast.castingPlayerId());
        }
        return ends(effect, Duration.End.CREATURE_SPELL_CAST) && cast.spellCard().hasType(CardType.CREATURE);
    }

    private static boolean ends(FloatingContinuousEffect effect, Duration.End end) {
        return effect.duration() != null && effect.duration().end() == end;
    }

    /** Whether {@code effect} began before the moment stamped {@code timestamp} (0: no such moment yet). */
    private static boolean startedBefore(FloatingContinuousEffect effect, long timestamp) {
        return timestamp > 0 && effect.timestamp() < timestamp;
    }

    /** Undoes what the ended {@code effect} kept on the permanent it affected, phased out or not. */
    private void undo(GameData gameData, FloatingContinuousEffect effect) {
        if (effect.effect() instanceof BombardmentEffect bombardment) {
            gameData.restoreBombardmentCards(bombardment.originalCards());
            return;
        }
        if (effect.affectedPermanentId() == null) {
            return;
        }
        Permanent affected = findPermanent(gameData, effect.affectedPermanentId());
        if (affected == null) {
            return;
        }
        switch (effect.effect()) {
            case PhasedOutHeldEffect held when held.phasesInWhenEnded() ->
                    phasingService.phaseInHeld(gameData, affected.getId(), held.tapsOnPhaseIn());
            case TapRestrictionEffect restriction when restriction.preventsTappingUnlessAttacking() ->
                    affected.removeTapRestriction(effect.id());
            case null, default -> {
            }
        }
    }

    /**
     * Reverts the copy effects from resolutions among {@code ended}, per permanent they affected, phased out or not:
     * those that ended together are judged together, so the copy left is the newest one that remains (CR 613.7).
     */
    private static void endCopies(GameData gameData, List<FloatingContinuousEffect> ended) {
        Map<UUID, List<FloatingContinuousEffect>> copiesByPermanent = new java.util.LinkedHashMap<>();
        for (FloatingContinuousEffect effect : ended) {
            if (effect.effect() instanceof ResolvedCopyEffect && effect.affectedPermanentId() != null) {
                copiesByPermanent.computeIfAbsent(effect.affectedPermanentId(), id -> new java.util.ArrayList<>()).add(effect);
            }
        }
        copiesByPermanent.forEach((permanentId, copies) -> {
            Permanent affected = findPermanent(gameData, permanentId);
            if (affected != null) {
                PermanentCopierService.endCopies(gameData, affected, copies);
            }
        });
    }

    private static Permanent findPermanent(GameData gameData, UUID permanentId) {
        for (Map<UUID, ? extends List<Permanent>> zone : List.of(gameData.playerBattlefields,
                gameData.phasedOutPermanents)) {
            for (List<Permanent> permanents : zone.values()) {
                for (Permanent permanent : permanents) {
                    if (permanent.getId().equals(permanentId)) {
                        return permanent;
                    }
                }
            }
        }
        return null;
    }
}
