package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext.DeathExileReplacement;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureCause;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.DepartureSnapshot;
import com.github.laxika.magicalvibes.model.DepartureStage;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Moves one {@link DepartureBatch} of permanents off the battlefield as a single event (CR 603.10a), answering
 * every question about how each of them leaves while all of them are still on the battlefield.
 *
 * <p>{@link #depart} runs four stages:
 * <ol>
 *   <li><b>Gather</b> ({@link DepartureStage#GATHER}): for destroyed permanents, indestructible (CR 702.12b) and
 *       the destruction replacements of CR 614.8 (regeneration, shield counters, Umbra armor, ...). A permanent
 *       with several asks its controller in APNAP order (CR 101.4). The permanents they save stay; the
 *       replacements themselves are applied only when the batch is performed, so nothing changes while any
 *       question of the batch is open.</li>
 *   <li><b>Choose</b> ({@link DepartureStage#CHOOSE}): for every permanent still leaving, its controller picks one
 *       of its competing death replacements (CR 616.1), or answers its lone "may" replacement, in APNAP order.</li>
 *   <li><b>Perform</b> ({@link DepartureStage#PERFORM}): the destruction replacements are applied, last-known
 *       information of every departing permanent is taken, then they all leave together, each with its chosen
 *       replacement.</li>
 *   <li><b>Report</b> ({@link DepartureStage#REPORT}): a {@link DepartureResult} per intent.</li>
 * </ol>
 *
 * <p>While a question is open, {@link #depart} returns {@link Outcome.Pending}: nothing has left, and the batch
 * waits in {@link GameData#pendingDepartureBatch} with the asked permanents in
 * {@link DepartureBatch#awaitingAnswerIds()}. The answer handlers record each answer on that batch
 * ({@link #recordDestructionAnswer}, {@link #recordReplacementAnswer}, {@link #recordMayLibraryAnswer}); once
 * none is outstanding, the caller passes the pending batch to {@link #depart} again and it resumes at its stage.
 * Because the batch is data in {@code GameData} and names its permanents by id, a simulation copy taken while a
 * question is open resumes on its own battlefield.
 */
@Service
public class DepartureService {

    /** What {@link #depart} did with a batch. */
    public sealed interface Outcome {

        /**
         * A question is open. Nothing in the batch has left the battlefield; the batch waits in
         * {@link GameData#pendingDepartureBatch} until it is answered.
         */
        record Pending() implements Outcome {}

        /**
         * The batch was performed.
         *
         * @param results     one result per intent, in the batch's intent order
         * @param survivorIds permanents a destruction replacement (indestructible, regeneration, ...) kept on
         *                    the battlefield
         */
        record Done(List<DepartureResult> results, Set<UUID> survivorIds) implements Outcome {
            public Done {
                results = List.copyOf(results);
                survivorIds = Set.copyOf(survivorIds);
            }
        }
    }

    /** Called for each permanent of a batch as it is performed, right after it left or failed to leave. */
    @FunctionalInterface
    public interface AfterEach {
        /**
         * @param permanent the permanent as it was on the battlefield
         * @param snapshot  its last-known information, or {@code null} when the event couldn't touch it
         * @param removed   whether it left the battlefield
         */
        void departed(DepartureIntent intent, Permanent permanent, DepartureSnapshot snapshot, boolean removed);
    }

    /**
     * What a caller runs while its batch is performed. Hooks are not part of the batch: a caller stopped for a
     * question passes them again when it is run again.
     *
     * @param beforePerform runs once every question is answered, before anything leaves or is saved
     * @param afterEach     runs for each permanent that didn't survive, in intent order, inside the event
     */
    public record Hooks(Runnable beforePerform, AfterEach afterEach) {
        public static final Hooks NONE = new Hooks(() -> { }, (intent, permanent, snapshot, removed) -> { });

        public static Hooks afterEach(AfterEach afterEach) {
            return new Hooks(NONE.beforePerform(), afterEach);
        }
    }

    private record Departing(DepartureIntent intent, Permanent permanent, UUID controllerId) {}

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardService graveyardService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    public DepartureService(@Lazy GameQueryService gameQueryService,
                            @Lazy PermanentRemovalService permanentRemovalService,
                            @Lazy GraveyardService graveyardService,
                            @Lazy TriggerCollectionService triggerCollectionService,
                            GameLogService gameLogService) {
        this.gameQueryService = gameQueryService;
        this.permanentRemovalService = permanentRemovalService;
        this.graveyardService = graveyardService;
        this.triggerCollectionService = triggerCollectionService;
        this.gameLogService = gameLogService;
    }

    /**
     * Runs {@code batch} from its stage: a new batch from {@link DepartureBatch#of}, or the pending batch once
     * its questions are answered. Returns {@link Outcome.Pending} while a question is open (the batch is then
     * the pending batch), and {@link Outcome.Done} once every permanent in it has left or stayed.
     *
     * <p>A batch without a state-based intent comes from a resolving effect or a choice completion, which can't
     * wait in the middle. It asks its questions only while that caller can be stopped and run again
     * ({@link GameData#deathReplacementsCanRerunEffect}): it then stays pending, the window is closed,
     * {@link GameData#rerunCurrentEffectAfterInteraction} is set and {@link DeathReplacementChoicePending} is
     * thrown, so the caller is parked and resolved (or completed) again once every answer is in. The rerun builds
     * the same batch, and this method continues the answered pending batch in its place. When the caller can't be
     * stopped, nothing is asked: each permanent leaves as it did before departure batches existed (a lone
     * question opens for it alone and it stays until answered). Such a batch therefore never returns
     * {@link Outcome.Pending}; see {@link #departNow}.
     */
    public Outcome depart(GameData gameData, DepartureBatch batch) {
        return depart(gameData, batch, Hooks.NONE);
    }

    /** {@link #depart(GameData, DepartureBatch)}, running {@code hooks} while the batch is performed. */
    public Outcome depart(GameData gameData, DepartureBatch batch, Hooks hooks) {
        boolean stateBased = batch.isStateBased();
        boolean owned = stateBased || batch.isOwnedByResolutionStep();
        if (!owned) {
            DepartureBatch answered = answeredRerunBatch(gameData, batch);
            if (answered != null) {
                batch = answered;
                owned = true;
            }
        }
        if (batch.isAwaitingAnswer()) {
            gameData.pendingDepartureBatch = batch;
            return new Outcome.Pending();
        }
        boolean canAsk = owned
                || gameData.deathReplacementsCanRerunEffect && (owned || gameData.pendingDepartureBatch == null);
        if (batch.stage() == DepartureStage.GATHER) {
            batch = gather(gameData, batch, canAsk);
            if (batch.isAwaitingAnswer()) {
                return paused(gameData, batch);
            }
        }
        if (batch.stage() == DepartureStage.CHOOSE) {
            batch = choose(gameData, batch, canAsk);
            if (batch.isAwaitingAnswer()) {
                return paused(gameData, batch);
            }
        }
        if (owned) {
            gameData.pendingDepartureBatch = null;
        }
        Map<UUID, List<UUID>> cardIds = new HashMap<>();
        batch = perform(gameData, batch, cardIds, hooks);
        return new Outcome.Done(report(gameData, batch, cardIds), batch.survivorIds());
    }

    /**
     * {@link #depart(GameData, DepartureBatch, Hooks)} for a batch without a state-based intent, which is either
     * performed at once or stops its caller with {@link DeathReplacementChoicePending}.
     */
    public Outcome.Done departNow(GameData gameData, DepartureBatch batch, Hooks hooks) {
        if (depart(gameData, batch, hooks) instanceof Outcome.Done done) {
            return done;
        }
        throw new IllegalStateException("A state-based departure batch can't be performed at once");
    }

    /**
     * Drops an answered pending batch that no rerun took over, once the window its caller was stopped in has
     * closed without stopping it again (the effect fizzled, or chose differently), so it doesn't hold up later
     * state-based checks.
     */
    public static void discardAnsweredRerunBatch(GameData gameData) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending != null && !pending.isAwaitingAnswer() && !pending.isStateBased()
                && !pending.isOwnedByResolutionStep()) {
            gameData.pendingDepartureBatch = null;
        }
    }

    /**
     * Asks, before a cost is paid, every CR 616.1 question the sacrifices of {@code batch} need (CR 601.2h: the
     * player pays the costs in any order, so the payment can stop here). Nothing has left when this returns. When a
     * question is opened, the batch waits in {@link GameData#pendingDepartureBatch} and the caller must stop before
     * paying anything else and store a {@link GameData.CostPaymentReplay}; once every answer is in, the replay takes
     * the answers with {@link #takeAnswersForPayment} and pays again, and the sacrifices consume them. A permanent
     * whose answer is already waiting for that payment is not asked again.
     *
     * @return {@code true} when a question is open
     */
    public boolean askBeforePayment(GameData gameData, DepartureBatch batch) {
        if (gameData.pendingDepartureBatch != null || gameData.pendingDeathReplacementChoiceReplay != null) {
            return false;
        }
        List<DepartureIntent> unanswered = batch.intents().stream()
                .filter(intent -> !gameData.chosenDeathReplacementsForRerun.containsKey(intent.permanentId()))
                .toList();
        if (unanswered.isEmpty()) {
            return false;
        }
        return choose(gameData, DepartureBatch.of(unanswered).withStage(DepartureStage.CHOOSE), true)
                .isAwaitingAnswer();
    }

    /**
     * Moves the answers of the answered batch {@link #askBeforePayment} asked into
     * {@link GameData#chosenDeathReplacementsForRerun}, for the payment that is made again to consume, and drops the
     * batch.
     *
     * @return the permanents that were answered for
     */
    public static Set<UUID> takeAnswersForPayment(GameData gameData) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending == null || pending.isAwaitingAnswer() || pending.isStateBased()
                || pending.isOwnedByResolutionStep()) {
            return Set.of();
        }
        gameData.pendingDepartureBatch = null;
        gameData.chosenDeathReplacementsForRerun.putAll(pending.replacementAnswers());
        return Set.copyOf(pending.replacementAnswers().keySet());
    }

    /**
     * Departs {@code batch} as {@code step}, the effect of {@code entry} being resolved, and records its results on
     * the entry's resolution scratchpad ({@link StackEntry#recordDepartures}) for the next steps to read.
     *
     * <p>While {@code step} is the resolution step itself ({@link GameData#stepDepartureEffect}: not an effect
     * another handler dispatches, and only the step's first departure) and no other batch is pending, a CR 616.1
     * question ends the step: the batch is owned by {@code entry} and left pending, nothing has moved, and
     * {@link Outcome.Pending} is returned. The handler must then return at once; it is not run again.
     * {@code EffectResolutionService} parks the resolution at the next step, and once every answer is in
     * {@link #finishStepDeparture} performs the batch, records its results and lets the resolution go on. Otherwise
     * the batch is departed through {@link #departNow} (asked and run again inside a marked handler's window, or
     * performed at once).
     *
     * <p>Whatever must happen as each permanent leaves is done here, the same way whether the batch was paused or
     * not, since the handler isn't there when it resumes: a sacrificed permanent is logged and fires its "whenever
     * you sacrifice" triggers, a destroyed one is logged, and Auras left attached to nothing are put into their
     * owners' graveyards afterwards. Anything else a later step needs it reads from the recorded
     * {@link DepartureResult}s.
     */
    public Outcome departAsStep(GameData gameData, StackEntry entry, CardEffect step, DepartureBatch batch) {
        boolean endsStep = step != null && gameData.stepDepartureEffect == step
                && gameData.pendingDepartureBatch == null;
        gameData.stepDepartureEffect = null;
        if (!endsStep) {
            Outcome.Done done = departNow(gameData, batch, stepHooks(gameData, batch));
            finishStep(gameData, entry, done);
            return done;
        }
        Outcome outcome = depart(gameData, batch.ownedBy(entry.getTargetableId()), stepHooks(gameData, batch));
        if (outcome instanceof Outcome.Done done) {
            finishStep(gameData, entry, done);
        }
        return outcome;
    }

    /** Whether the pending departure batch belongs to a step of {@code entry} (see {@link #departAsStep}). */
    public static boolean awaitsStepDeparture(GameData gameData, StackEntry entry) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        return pending != null && pending.isOwnedByResolutionStep()
                && pending.ownerStackEntryId().equals(entry.getTargetableId());
    }

    /**
     * Resumes the departure a step of {@code entry} ended on (see {@link #departAsStep}) before its resolution goes
     * on with the next step: performs the batch from its stage and records its results on {@code entry}. Returns
     * {@code false} while a question of the batch is still open, so the resolution stays parked.
     */
    public boolean finishStepDeparture(GameData gameData, StackEntry entry) {
        if (!awaitsStepDeparture(gameData, entry)) {
            return true;
        }
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (depart(gameData, pending, stepHooks(gameData, pending)) instanceof Outcome.Done done) {
            finishStep(gameData, entry, done);
            return true;
        }
        return false;
    }

    private Hooks stepHooks(GameData gameData, DepartureBatch batch) {
        return new Hooks(() -> {
            for (Departing departing : locate(gameData, batch, intent -> intent.cause() == DepartureCause.DESTROY)) {
                if (gameQueryService.hasKeyword(gameData, departing.permanent(), Keyword.INDESTRUCTIBLE)) {
                    gameLogService.append(gameData, GameLog.isIndestructible(departing.permanent().getCard()));
                }
            }
        }, (intent, permanent, snapshot, removed) -> {
            if (!removed) {
                return;
            }
            if (intent.cause() == DepartureCause.SACRIFICE) {
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, snapshot.controllerId(),
                        permanent.getCard(), null, snapshot.creature());
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is sacrificed."));
            } else if (intent.cause() == DepartureCause.DESTROY) {
                gameLogService.append(gameData, GameLog.isDestroyed(permanent.getCard()));
            }
        });
    }

    private void finishStep(GameData gameData, StackEntry entry, Outcome.Done done) {
        permanentRemovalService.removeOrphanedAuras(gameData);
        entry.recordDepartures(done.results());
    }

    /**
     * The answered pending batch a rerun of the caller that asked it continues, or {@code null}. A stale one with
     * other intents is dropped.
     */
    private static DepartureBatch answeredRerunBatch(GameData gameData, DepartureBatch batch) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending == null || pending.isAwaitingAnswer() || pending.isStateBased()
                || pending.isOwnedByResolutionStep()) {
            return null;
        }
        if (pending.intents().equals(batch.intents())) {
            return pending;
        }
        gameData.pendingDepartureBatch = null;
        return null;
    }

    private static Outcome paused(GameData gameData, DepartureBatch batch) {
        if (batch.isStateBased() || batch.isOwnedByResolutionStep()) {
            return new Outcome.Pending();
        }
        gameData.deathReplacementsCanRerunEffect = false;
        gameData.rerunCurrentEffectAfterInteraction = true;
        throw new DeathReplacementChoicePending();
    }

    /**
     * Records the destruction replacement {@code permanentId}'s controller chose for the pending batch.
     * Returns {@code false} when the pending batch did not ask about it.
     */
    public boolean recordDestructionAnswer(GameData gameData, UUID permanentId, String option) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending == null || !pending.awaitingAnswerIds().contains(permanentId)) {
            return false;
        }
        gameData.pendingDepartureBatch = pending.withDestructionAnswer(permanentId, option);
        answeredForRerun(gameData, pending);
        return true;
    }

    /**
     * Records the CR 616.1 death replacement labelled {@code label} that {@code permanentId}'s controller chose
     * from {@code options} for the pending batch. Returns {@code false} when the pending batch did not ask about it.
     */
    public boolean recordReplacementAnswer(GameData gameData, UUID permanentId,
                                           List<DeathExileReplacement> options, String label) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending == null || !pending.awaitingAnswerIds().contains(permanentId)) {
            return false;
        }
        DeathExileReplacement chosen = PermanentRemovalService.chosenOption(options, label);
        Permanent permanent = findOnBattlefield(gameData, permanentId);
        if (permanent != null) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    ": its controller chooses \"" + chosen.label() + "\" as the replacement effect."));
        }
        gameData.pendingDepartureBatch = pending.withReplacementAnswer(permanentId, chosen);
        answeredForRerun(gameData, pending);
        return true;
    }

    /**
     * Records the answer to the "you may put it on top of its owner's library instead" asked for
     * {@code permanentId} for the pending batch. Returns {@code false} when the pending batch did not ask about
     * it, so the caller applies the answer at once.
     */
    public boolean recordMayLibraryAnswer(GameData gameData, UUID permanentId, boolean accepted) {
        DepartureBatch pending = gameData.pendingDepartureBatch;
        if (pending == null || !pending.awaitingAnswerIds().contains(permanentId)) {
            return false;
        }
        gameData.pendingDepartureBatch = pending.withReplacementAnswer(permanentId,
                PermanentRemovalService.mayLibraryAnswer(accepted));
        answeredForRerun(gameData, pending);
        return true;
    }

    /**
     * The resolving effect a batch without a state-based intent stopped was parked to resolve again; it must not
     * rerun twice. A stopped choice completion is completed again instead and leaves the parked effect alone.
     */
    private static void answeredForRerun(GameData gameData, DepartureBatch batch) {
        if (!batch.isStateBased() && !batch.isOwnedByResolutionStep()
                && gameData.pendingDeathReplacementChoiceReplay == null) {
            gameData.rerunCurrentEffectAfterInteraction = false;
        }
    }

    private DepartureBatch gather(GameData gameData, DepartureBatch batch, boolean canAsk) {
        DepartureBatch gathering = batch;
        List<Departing> destroyed = locate(gameData, gathering,
                intent -> intent.cause().isDestruction() && !intent.destructionReplacementsApplied())
                .stream().filter(departing -> !isUnaffected(gameData, departing)).toList();
        Map<Departing, Map<String, String>> questions = new LinkedHashMap<>();
        if (canAsk) {
            for (Departing departing : inApnapOrder(gameData, destroyed)) {
                if (gathering.destructionAnswers().containsKey(departing.intent().permanentId())
                        || isIndestructible(gameData, departing)) {
                    continue;
                }
                Map<String, String> options = destructionReplacementOptions(gameData, departing);
                if (options.size() > 1) {
                    questions.put(departing, options);
                    batch = batch.awaitingAnswer(departing.intent().permanentId());
                }
            }
        }
        if (!questions.isEmpty()) {
            gameData.pendingDepartureBatch = batch;
            questions.forEach((departing, options) ->
                    graveyardService.askDepartureDestructionReplacement(gameData, departing.permanent(), options));
            return batch;
        }
        // Nothing is applied yet: a permanent a replacement will keep survives, and the replacements are applied
        // when the batch is performed, so a caller stopped for a later question has still changed nothing.
        for (Departing departing : destroyed) {
            if (isIndestructible(gameData, departing)
                    || batch.destructionAnswers().containsKey(departing.intent().permanentId())
                    || !destructionReplacementOptions(gameData, departing).isEmpty()) {
                batch = batch.survivor(departing.intent().permanentId());
            }
        }
        return batch.withStage(DepartureStage.CHOOSE);
    }

    /** CR 122.1c: shield counters replace only destruction by an effect, not by a state-based action. */
    private Map<String, String> destructionReplacementOptions(GameData gameData, Departing departing) {
        DepartureIntent intent = departing.intent();
        return graveyardService.destructionReplacementOptions(gameData, departing.permanent(),
                !intent.cantBeRegenerated(), intent.cause() == DepartureCause.DESTROY);
    }

    /** Applies the destruction replacement that keeps {@code departing}; returns whether one still did. */
    private boolean applyDestructionReplacement(GameData gameData, DepartureBatch batch, Departing departing) {
        if (isIndestructible(gameData, departing)) {
            return true;
        }
        DepartureIntent intent = departing.intent();
        String answer = batch.destructionAnswers().get(intent.permanentId());
        if (answer != null) {
            graveyardService.applyDestructionReplacement(gameData, departing.permanent(), answer);
            return true;
        }
        return intent.cause() == DepartureCause.DESTROY
                ? graveyardService.tryReplaceDestruction(gameData, departing.permanent(), !intent.cantBeRegenerated())
                : graveyardService.tryRegenerate(gameData, departing.permanent());
    }

    /**
     * Whether a destroy or sacrifice can't touch the permanent at all (its controller's own effects can't affect
     * it, or a triggered ability can't move the creature token): nothing is asked about it and it stays.
     */
    private boolean isUnaffected(GameData gameData, Departing departing) {
        DepartureCause cause = departing.intent().cause();
        if (cause != DepartureCause.DESTROY && cause != DepartureCause.SACRIFICE) {
            return false;
        }
        Permanent permanent = departing.permanent();
        return gameQueryService.cantBeAffectedByOwnEffects(gameData, permanent, gameData.currentlyResolvingControllerId)
                || cause == DepartureCause.SACRIFICE
                        && !gameQueryService.triggeredAbilityCanMoveCreatureToken(gameData, permanent);
    }

    private boolean isIndestructible(GameData gameData, Departing departing) {
        return gameQueryService.hasKeyword(gameData, departing.permanent(), Keyword.INDESTRUCTIBLE);
    }

    private DepartureBatch choose(GameData gameData, DepartureBatch batch, boolean canAsk) {
        // A caller that asked its deaths ahead of the batch the older way (askDeathReplacementsBeforeRemoval) has
        // its answers waiting for the rerun; they answer the batch's question too.
        for (DepartureIntent intent : batch.intents()) {
            DeathExileReplacement earlier = gameData.chosenDeathReplacementsForRerun.remove(intent.permanentId());
            if (earlier != null && !batch.survivorIds().contains(intent.permanentId())) {
                batch = batch.withReplacementAnswer(intent.permanentId(), earlier);
            }
        }
        if (!canAsk) {
            return batch.withStage(DepartureStage.PERFORM);
        }
        DepartureBatch choosing = batch;
        Map<Departing, List<DeathExileReplacement>> questions = new LinkedHashMap<>();
        for (Departing departing : inApnapOrder(gameData, locate(gameData, choosing, intent ->
                !choosing.survivorIds().contains(intent.permanentId())
                        && !choosing.replacementAnswers().containsKey(intent.permanentId())))) {
            if (isUnaffected(gameData, departing)) {
                continue;
            }
            List<DeathExileReplacement> options =
                    permanentRemovalService.deathExileReplacementOptions(gameData, departing.permanent());
            if (options.size() > 1) {
                questions.put(departing, options);
            } else if (permanentRemovalService.hasMayLibraryReplacement(gameData, departing.permanent())) {
                // Its only replacement is a "may": asked through the usual may prompt.
                questions.put(departing, List.of());
            } else {
                continue;
            }
            batch = batch.awaitingAnswer(departing.intent().permanentId());
        }
        if (questions.isEmpty()) {
            return batch.withStage(DepartureStage.PERFORM);
        }
        gameData.pendingDepartureBatch = batch;
        permanentRemovalService.beginDeathReplacementChoiceBatch(gameData);
        try {
            questions.forEach((departing, options) -> {
                if (options.isEmpty()) {
                    permanentRemovalService.offerMayLibraryReplacement(gameData, departing.permanent());
                } else {
                    permanentRemovalService.askDepartureDeathReplacement(gameData, departing.permanent(), options,
                            departing.intent());
                }
            });
        } finally {
            permanentRemovalService.endDeathReplacementChoiceBatch(gameData);
        }
        return batch;
    }

    private DepartureBatch perform(GameData gameData, DepartureBatch batch, Map<UUID, List<UUID>> cardIds,
                                   Hooks hooks) {
        // Something now changes, so a caller stopped later can't be run again from the start.
        gameData.deathReplacementsCanRerunEffect = false;
        hooks.beforePerform().run();
        DepartureBatch saving = batch;
        for (Departing survivor : locate(gameData, saving,
                intent -> saving.survivorIds().contains(intent.permanentId()))) {
            if (!applyDestructionReplacement(gameData, saving, survivor)) {
                batch = batch.withoutSurvivor(survivor.intent().permanentId());
            }
        }
        DepartureBatch performing = batch;
        List<Departing> departing = locate(gameData, performing,
                intent -> !performing.survivorIds().contains(intent.permanentId()));
        for (Departing leaving : departing) {
            if (isUnaffected(gameData, leaving)) {
                continue;
            }
            batch = batch.withSnapshot(snapshot(gameData, leaving));
            Permanent permanent = leaving.permanent();
            List<UUID> ids = new ArrayList<>(2);
            if (permanent.getOriginalCard() != null) {
                ids.add(permanent.getOriginalCard().getId());
            }
            ids.add(permanent.getCard().getId());
            cardIds.put(leaving.intent().permanentId(), ids);
        }
        batch = batch.withStage(DepartureStage.PERFORM);

        Map<UUID, Permanent> oldPermanents = new HashMap<>(gameData.simultaneousDyingPermanents);
        Map<UUID, UUID> oldPermanentControllers = new HashMap<>(gameData.simultaneousDyingPermanentControllers);
        Map<UUID, Permanent> oldCreatures = new HashMap<>(gameData.simultaneousDyingCreatures);
        Map<UUID, UUID> oldCreatureControllers = new HashMap<>(gameData.simultaneousDyingControllers);
        Map<UUID, Integer> oldPowers = new HashMap<>(gameData.simultaneousDyingPowers);
        Map<UUID, Map<Keyword, Integer>> oldKeywordInstances =
                new HashMap<>(gameData.simultaneousDyingTriggerKeywordInstances);
        Map<UUID, List<CardEffect>> oldCreatureDeathEffects =
                new HashMap<>(gameData.simultaneousDyingGrantedCreatureDeathEffects);
        Map<UUID, List<CardEffect>> oldSelfDeathEffects =
                new HashMap<>(gameData.simultaneousDyingGrantedSelfDeathEffects);
        batch.snapshots().values().forEach(snapshot -> publish(gameData, snapshot));
        // Leaving together, so "whenever one or more ... leave" watchers that also leave still see the others leave.
        boolean leaveBatch = departing.size() > 1;
        if (leaveBatch) {
            permanentRemovalService.beginPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.beginDeathReplacementChoiceBatch(gameData);
        try {
            for (Departing leaving : departing) {
                DepartureSnapshot snapshot = batch.snapshots().get(leaving.intent().permanentId());
                boolean removed = snapshot != null && permanentRemovalService.departToGraveyard(gameData,
                        leaving.permanent(), leaving.intent(),
                        batch.replacementAnswers().get(leaving.intent().permanentId()));
                hooks.afterEach().departed(leaving.intent(), leaving.permanent(), snapshot, removed);
            }
            triggerCollectionService.checkBatchedAllyArtifactOrCreatureDeathTriggers(gameData);
            triggerCollectionService.checkBatchedAllyCreatureDeathTriggers(gameData);
        } finally {
            restore(gameData.simultaneousDyingPermanents, oldPermanents);
            restore(gameData.simultaneousDyingPermanentControllers, oldPermanentControllers);
            restore(gameData.simultaneousDyingCreatures, oldCreatures);
            restore(gameData.simultaneousDyingControllers, oldCreatureControllers);
            restore(gameData.simultaneousDyingPowers, oldPowers);
            restore(gameData.simultaneousDyingTriggerKeywordInstances, oldKeywordInstances);
            restore(gameData.simultaneousDyingGrantedCreatureDeathEffects, oldCreatureDeathEffects);
            restore(gameData.simultaneousDyingGrantedSelfDeathEffects, oldSelfDeathEffects);
            permanentRemovalService.endDeathReplacementChoiceBatch(gameData);
            if (leaveBatch) {
                permanentRemovalService.endPermanentLeaveBatch(gameData);
            }
        }
        return batch.withStage(DepartureStage.REPORT);
    }

    private DepartureSnapshot snapshot(GameData gameData, Departing departing) {
        Permanent permanent = departing.permanent();
        Permanent lastKnown = new Permanent(permanent);
        lastKnown.setLosesAllAbilitiesUntilEndOfTurn(gameQueryService.hasLostPrintedAbilities(gameData, permanent));
        lastKnown.setLastKnownPower(gameQueryService.getEffectivePower(gameData, permanent));
        lastKnown.setLastKnownToughness(gameQueryService.getEffectiveToughness(gameData, permanent));
        for (StackEntry stackEntry : gameData.stack) {
            Permanent stackSource = stackEntry.getSourcePermanentSnapshot();
            if (permanent.getId().equals(stackEntry.getSourcePermanentId())
                    && stackSource != null && stackSource.getLastKnownPower() == null) {
                stackSource.setLastKnownPower(lastKnown.getLastKnownPower());
                stackSource.setLastKnownToughness(lastKnown.getLastKnownToughness());
            }
        }
        lastKnown.setCard(permanentRemovalService.snapshotEffectivePermanentCard(gameData, permanent));
        permanentRemovalService.snapshotEffectiveSubtypes(gameData, lastKnown);
        List<CardEffect> selfDeathEffects =
                triggerCollectionService.grantedTriggeredEffects(gameData, permanent, EffectSlot.ON_DEATH);
        boolean creature = gameQueryService.isCreature(gameData, permanent);
        if (!creature) {
            return new DepartureSnapshot(lastKnown, departing.controllerId(), false, Map.of(), List.of(),
                    selfDeathEffects);
        }
        List<CardEffect> creatureDeathEffects =
                new ArrayList<>(permanent.getTemporaryTriggeredEffects(EffectSlot.ON_ANY_CREATURE_DIES));
        creatureDeathEffects.addAll(triggerCollectionService.grantedTriggeredEffects(
                gameData, permanent, EffectSlot.ON_ANY_CREATURE_DIES));
        return new DepartureSnapshot(lastKnown, departing.controllerId(), true, Map.of(
                Keyword.PERSIST, gameQueryService.triggerKeywordInstances(gameData, permanent, Keyword.PERSIST),
                Keyword.UNDYING, gameQueryService.triggerKeywordInstances(gameData, permanent, Keyword.UNDYING)),
                creatureDeathEffects, selfDeathEffects);
    }

    /** Exposes a snapshot through the {@code simultaneousDying*} maps the removal and trigger code read. */
    private static void publish(GameData gameData, DepartureSnapshot snapshot) {
        UUID permanentId = snapshot.permanentId();
        gameData.simultaneousDyingGrantedSelfDeathEffects.put(permanentId, snapshot.grantedSelfDeathEffects());
        gameData.simultaneousDyingPermanents.put(permanentId, snapshot.permanent());
        gameData.simultaneousDyingPermanentControllers.put(permanentId, snapshot.controllerId());
        if (!snapshot.creature()) {
            return;
        }
        gameData.simultaneousDyingCreatures.put(permanentId, snapshot.permanent());
        gameData.simultaneousDyingControllers.put(permanentId, snapshot.controllerId());
        gameData.simultaneousDyingPowers.put(permanentId, snapshot.permanent().getLastKnownPower());
        gameData.simultaneousDyingTriggerKeywordInstances.put(permanentId, snapshot.triggerKeywordInstances());
        gameData.simultaneousDyingGrantedCreatureDeathEffects.put(permanentId, snapshot.grantedCreatureDeathEffects());
    }

    private static <V> void restore(Map<UUID, V> map, Map<UUID, V> previous) {
        map.clear();
        map.putAll(previous);
    }

    private List<DepartureResult> report(GameData gameData, DepartureBatch batch, Map<UUID, List<UUID>> cardIds) {
        List<DepartureResult> results = new ArrayList<>(batch.intents().size());
        for (DepartureIntent intent : batch.intents()) {
            UUID permanentId = intent.permanentId();
            DepartureSnapshot snapshot = batch.snapshots().get(permanentId);
            if (snapshot == null) {
                boolean stayed = findOnBattlefield(gameData, permanentId) != null;
                results.add(new DepartureResult(permanentId, stayed ? Zone.BATTLEFIELD : null, null, null, null, null,
                        false, false, stayed ? gameQueryService.findPermanentController(gameData, permanentId) : null));
                continue;
            }
            Integer power = snapshot.creature() ? snapshot.permanent().getLastKnownPower() : null;
            Integer toughness = snapshot.creature() ? snapshot.permanent().getLastKnownToughness() : null;
            if (findOnBattlefield(gameData, permanentId) != null) {
                results.add(new DepartureResult(permanentId, Zone.BATTLEFIELD, null, null, power, toughness,
                        false, false, snapshot.controllerId(), lastKnownCardTypes(snapshot)));
                continue;
            }
            Location location = cardIds.getOrDefault(permanentId, List.of()).stream()
                    .map(cardId -> locateCard(gameData, cardId))
                    .filter(found -> found != null)
                    .findFirst()
                    .orElse(null);
            results.add(new DepartureResult(permanentId, location == null ? null : location.zone(),
                    location == null ? null : location.ownerId(), location == null ? null : location.cardId(),
                    power, toughness, intent.cause().isDestruction(), intent.cause() == DepartureCause.SACRIFICE,
                    snapshot.controllerId(), lastKnownCardTypes(snapshot)));
        }
        return results;
    }

    /** The card types of the snapshot's last-known card, which holds its effective types (CR 608.2h). */
    private static Set<CardType> lastKnownCardTypes(DepartureSnapshot snapshot) {
        Card card = snapshot.permanent().getCard();
        Set<CardType> types = EnumSet.noneOf(CardType.class);
        if (card == null) {
            return types;
        }
        if (card.getType() != null) {
            types.add(card.getType());
        }
        types.addAll(card.getAdditionalTypes());
        return types;
    }

    private record Location(Zone zone, UUID ownerId, UUID cardId) {}

    private static Location locateCard(GameData gameData, UUID cardId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (containsCard(gameData.playerGraveyards.get(playerId), cardId)) {
                return new Location(Zone.GRAVEYARD, playerId, cardId);
            }
        }
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled != null) {
            return new Location(Zone.EXILE, exiled.ownerId(), cardId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (containsCard(gameData.playerHands.get(playerId), cardId)) {
                return new Location(Zone.HAND, playerId, cardId);
            }
            if (containsCard(gameData.playerDecks.get(playerId), cardId)) {
                return new Location(Zone.LIBRARY, playerId, cardId);
            }
        }
        return null;
    }

    private static boolean containsCard(List<Card> zone, UUID cardId) {
        return zone != null && zone.stream().anyMatch(card -> card.getId().equals(cardId));
    }

    /** The intents matching {@code filter} whose permanent is still on the battlefield, in intent order. */
    private static List<Departing> locate(GameData gameData, DepartureBatch batch, Predicate<DepartureIntent> filter) {
        List<Departing> located = new ArrayList<>();
        for (DepartureIntent intent : batch.intents()) {
            if (!filter.test(intent)) {
                continue;
            }
            for (UUID playerId : gameData.orderedPlayerIds) {
                Permanent permanent = findIn(gameData.playerBattlefields.get(playerId), intent.permanentId());
                if (permanent != null) {
                    located.add(new Departing(intent, permanent, playerId));
                    break;
                }
            }
        }
        return located;
    }

    private static Permanent findOnBattlefield(GameData gameData, UUID permanentId) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            Permanent permanent = findIn(gameData.playerBattlefields.get(playerId), permanentId);
            if (permanent != null) {
                return permanent;
            }
        }
        return null;
    }

    private static Permanent findIn(List<Permanent> battlefield, UUID permanentId) {
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getId().equals(permanentId)) {
                return permanent;
            }
        }
        return null;
    }

    /** Orders {@code departing} by its controllers in APNAP order (CR 101.4), keeping intent order per player. */
    private static List<Departing> inApnapOrder(GameData gameData, List<Departing> departing) {
        int playerCount = gameData.orderedPlayerIds.size();
        int activeIndex = Math.max(0, gameData.orderedPlayerIds.indexOf(gameData.activePlayerId));
        List<Departing> ordered = new ArrayList<>(departing);
        ordered.sort(Comparator.comparingInt(leaving -> Math.floorMod(
                gameData.orderedPlayerIds.indexOf(leaving.controllerId()) - activeIndex, playerCount)));
        return ordered;
    }
}
