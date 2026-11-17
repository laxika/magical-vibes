package com.github.laxika.magicalvibes.service.duration;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NextMatchingSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TapRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.TemporaryCopyEffect;
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
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
 * undoes what the effect kept on the permanent it affected.
 *
 * <p>The events that end durations: a turn beginning ({@link TurnEvent.Began}: "until your next turn"), a step or phase
 * ending ({@link TurnEvent.StepEnded}: "until end of combat", "until the end of your next upkeep", CR 500.5), the
 * cleanup step's damage removal ({@link TurnEvent.Cleanup}: "until end of turn", "until the end of your next turn",
 * CR 514.2), a step beginning ({@link StepEvent}: "until your next upkeep", "until the next end step"), the untap step's
 * untapping ("until its controller's next untap step"), a spell being cast ({@link SpellEvent.Cast}) and a planeswalk
 * ({@link ActionEvent.Planeswalked}). A "for as long as" duration (CR 611.2b) ends where its condition stops holding,
 * not here.
 */
@Service
public class DurationClock {

    private final PredicateEvaluationService predicateEvaluationService;

    private final GameQueryService gameQueryService;

    public DurationClock(@Lazy PredicateEvaluationService predicateEvaluationService,
                         @Lazy GameQueryService gameQueryService) {
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
    }

    /** Ends the durations the events of {@code batch} end, in event order. */
    public void advance(GameData gameData, RulesEventBatch batch) {
        for (RulesEvent event : batch.events()) {
            advance(gameData, event);
        }
    }

    /** Ends the floating effects whose duration {@code event} ends, and returns them. */
    public List<FloatingContinuousEffect> advance(GameData gameData, RulesEvent event) {
        Predicate<FloatingContinuousEffect> ends = endsAt(gameData, event);
        if (ends == null) {
            return List.of();
        }
        List<FloatingContinuousEffect> ended = gameData.expireFloatingEffects(ends);
        for (FloatingContinuousEffect effect : ended) {
            undo(gameData, effect);
        }
        return ended;
    }

    /** Which floating effects {@code event} ends, or {@code null} when it ends none. */
    private Predicate<FloatingContinuousEffect> endsAt(GameData gameData, RulesEvent event) {
        return switch (event) {
            case TurnEvent.Began began -> effect -> ends(effect, Duration.End.YOUR_NEXT_TURN)
                    && began.activePlayerId().equals(effect.controllerId());
            case TurnEvent.StepEnded ended when ended.step() == TurnStep.END_OF_COMBAT ->
                    effect -> ends(effect, Duration.End.END_OF_COMBAT);
            case TurnEvent.StepEnded ended when ended.step() == TurnStep.UPKEEP && ended.activePlayerId() != null ->
                    effect -> ends(effect, Duration.End.END_OF_YOUR_NEXT_UPKEEP)
                            && ended.activePlayerId().equals(effect.controllerId())
                            && startedBefore(effect, gameData.upkeepStartTimestamp);
            case TurnEvent.Cleanup cleanup -> effect -> endsAtCleanup(gameData, cleanup.activePlayerId(), effect);
            case StepEvent step when step.moment() == StepTrigger.Moment.UPKEEP ->
                    effect -> ends(effect, Duration.End.YOUR_NEXT_UPKEEP)
                            && step.activePlayerId().equals(effect.controllerId());
            case StepEvent step when step.moment() == StepTrigger.Moment.END_STEP ->
                    effect -> ends(effect, Duration.End.NEXT_END_STEP);
            case TapEvent.UntappedDuringUntapStep untapped ->
                    effect -> ends(effect, Duration.End.AFFECTED_CONTROLLERS_NEXT_UNTAP_STEP)
                            && effect.affectedPermanentId() != null
                            && untapped.controllerId().equals(
                            gameQueryService.findPermanentController(gameData, effect.affectedPermanentId()));
            case SpellEvent.Cast cast -> effect -> endsAtCast(gameData, cast, effect);
            case ActionEvent.Planeswalked ignored -> effect -> ends(effect, Duration.End.PLANESWALK);
            default -> null;
        };
    }

    /**
     * Whether the cleanup step in {@code activePlayerId}'s turn ends {@code effect}: an "until end of turn" effect, a
     * reduction of the next matching spell that lasts the turn, and an "until the end of your next turn" effect of
     * the active player that began before this turn (CR 514.2). While players take their phases one after another
     * (Runed Terror), a turn's effects end only at the cleanup step of the player who was active as they began.
     */
    private boolean endsAtCleanup(GameData gameData, UUID activePlayerId, FloatingContinuousEffect effect) {
        if (ends(effect, Duration.End.END_OF_YOUR_NEXT_TURN)) {
            return activePlayerId != null && activePlayerId.equals(effect.controllerId())
                    && startedBefore(effect, gameData.turnStartTimestamp);
        }
        boolean endsThisTurn = ends(effect, Duration.End.END_OF_TURN)
                || (ends(effect, Duration.End.MATCHING_SPELL_CAST)
                && !(effect.effect() instanceof ReduceCastCostForNextMatchingSpellEffect reduction
                && !reduction.expiresAtCleanup()));
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
     * a cost change of the next spell its effect matches, and an effect that lasts until a creature spell is cast.
     */
    private boolean endsAtCast(GameData gameData, SpellEvent.Cast cast, FloatingContinuousEffect effect) {
        if (ends(effect, Duration.End.SOURCE_CARD_CAST_FROM_EXILE)) {
            return cast.castZone() == Zone.EXILE
                    && effect.effect() instanceof GrantActivatedAbilityEffect grant
                    && cast.spellCard().getId().equals(grant.expirationCardId());
        }
        if (ends(effect, Duration.End.MATCHING_SPELL_CAST)
                || ends(effect, Duration.End.TARGET_PLAYER_MATCHING_SPELL_CAST)) {
            return effect.effect() instanceof NextMatchingSpellCostEffect next
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
        if (effect.affectedPermanentId() == null) {
            return;
        }
        Permanent affected = findPermanent(gameData, effect.affectedPermanentId());
        if (affected == null) {
            return;
        }
        switch (effect.effect()) {
            case TemporaryCopyEffect ignored -> affected.revertEndOfTurnCopy();
            case TapRestrictionEffect restriction when restriction.preventsTappingUnlessAttacking() ->
                    affected.removeTapRestriction(effect.id());
            case GrantKeywordEffect grant when grant.duration() == GrantDuration.UNTIL_YOUR_NEXT_TURN ->
                    // The grant ends at its controller's turn even when the creature changed controllers, so the
                    // keywords mirrored on the permanent can't wait for its own controller's turn.
                    affected.getUntilNextTurnKeywords().removeAll(grant.keywords());
            case MakeTargetCopyOfTargetCreatureUntilNextTurnEffect ignored -> {
                // A newer copy effect overwrites the copy's controller, so an older one ending first must not revert
                // the card out from under the newer one.
                if (affected.hasUntilNextTurnCopyLayers()) {
                    affected.expireUntilNextTurnCopyLayers(effect.controllerId());
                } else if (affected.isCopyUntilControllerNextTurn()
                        && effect.controllerId().equals(affected.getCopyUntilNextTurnControllerId())) {
                    affected.revertUntilNextTurnCopy();
                }
            }
            case null, default -> {
            }
        }
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
