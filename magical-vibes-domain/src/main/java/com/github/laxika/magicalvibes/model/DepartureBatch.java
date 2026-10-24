package com.github.laxika.magicalvibes.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Permanents leaving the battlefield in one event (the "same event" of CR 603.10a). Every question about
 * how they leave is answered while they are all still on the battlefield, so the batch can be pending in
 * {@code GameData} between a question and its answer and resume at {@link #stage()}. Immutable: each
 * answer produces a new batch. Collections keep insertion order so simulations stay deterministic.
 *
 * @param intents              the permanents leaving and why, in the order they were added
 * @param stage                the stage reached; a pending batch resumes here
 * @param destructionAnswers   chosen destruction replacement option (as the regeneration choice names it)
 *                             by permanent id, answered in the {@link DepartureStage#GATHER} stage
 * @param replacementAnswers   chosen CR 616.1 death replacement by permanent id, answered in the
 *                             {@link DepartureStage#CHOOSE} stage; a declined "may" is
 *                             {@link ChoiceContext.DeathExileReplacement.Kind#NO_REPLACEMENT}
 * @param awaitingAnswerIds    permanents whose question has been asked and not yet answered
 * @param survivorIds          permanents a destruction replacement (indestructible, regeneration, ...) keeps on
 *                             the battlefield, found in the {@link DepartureStage#GATHER} stage and applied
 *                             when the batch is performed; they don't leave
 * @param snapshots            last-known information of each departing permanent by permanent id, taken
 *                             immediately before the batch is performed
 * @param ownerStackEntryId    the {@link StackEntry#getTargetableId() stack entry} whose resolution step departs
 *                             this batch and is parked until it is done (its results are then recorded on that
 *                             entry and the resolution goes on with the next step), or {@code null} when no
 *                             resolution step owns it. An id rather than the entry, so a simulation copy finds its
 *                             own parked entry
 */
public record DepartureBatch(List<DepartureIntent> intents, DepartureStage stage,
                             Map<UUID, String> destructionAnswers,
                             Map<UUID, ChoiceContext.DeathExileReplacement> replacementAnswers,
                             Set<UUID> awaitingAnswerIds, Set<UUID> survivorIds,
                             Map<UUID, DepartureSnapshot> snapshots, UUID ownerStackEntryId) {

    public DepartureBatch {
        Objects.requireNonNull(stage, "stage");
        intents = List.copyOf(intents);
        destructionAnswers = orderedCopy(destructionAnswers);
        replacementAnswers = orderedCopy(replacementAnswers);
        awaitingAnswerIds = Collections.unmodifiableSet(new LinkedHashSet<>(awaitingAnswerIds));
        survivorIds = Collections.unmodifiableSet(new LinkedHashSet<>(survivorIds));
        snapshots = orderedCopy(snapshots);
    }

    /** A new batch of {@code intents} that has not asked anything yet. */
    public static DepartureBatch of(List<DepartureIntent> intents) {
        return new DepartureBatch(intents, DepartureStage.GATHER, Map.of(), Map.of(), Set.of(), Set.of(),
                Map.of(), null);
    }

    /** This batch, departed by the resolution step of the stack entry {@code stackEntryId}. */
    public DepartureBatch ownedBy(UUID stackEntryId) {
        return new DepartureBatch(intents, stage, destructionAnswers, replacementAnswers, awaitingAnswerIds,
                survivorIds, snapshots, stackEntryId);
    }

    /** Whether a resolution step departs this batch and waits for it at the end of the step. */
    public boolean isOwnedByResolutionStep() {
        return ownerStackEntryId != null;
    }

    public DepartureBatch withStage(DepartureStage newStage) {
        return new DepartureBatch(intents, newStage, destructionAnswers, replacementAnswers, awaitingAnswerIds,
                survivorIds, snapshots, ownerStackEntryId);
    }

    /** Records that {@code permanentId}'s controller has been asked a question about it. */
    public DepartureBatch awaitingAnswer(UUID permanentId) {
        Set<UUID> awaiting = new LinkedHashSet<>(awaitingAnswerIds);
        awaiting.add(permanentId);
        return new DepartureBatch(intents, stage, destructionAnswers, replacementAnswers, awaiting, survivorIds,
                snapshots, ownerStackEntryId);
    }

    public DepartureBatch withDestructionAnswer(UUID permanentId, String option) {
        Map<UUID, String> answers = new LinkedHashMap<>(destructionAnswers);
        answers.put(permanentId, option);
        return new DepartureBatch(intents, stage, answers, replacementAnswers, answered(permanentId), survivorIds,
                snapshots, ownerStackEntryId);
    }

    public DepartureBatch withReplacementAnswer(UUID permanentId, ChoiceContext.DeathExileReplacement replacement) {
        Map<UUID, ChoiceContext.DeathExileReplacement> answers = new LinkedHashMap<>(replacementAnswers);
        answers.put(permanentId, replacement);
        return new DepartureBatch(intents, stage, destructionAnswers, answers, answered(permanentId), survivorIds,
                snapshots, ownerStackEntryId);
    }

    /** Records that a destruction replacement kept {@code permanentId} on the battlefield. */
    public DepartureBatch survivor(UUID permanentId) {
        Set<UUID> survivors = new LinkedHashSet<>(survivorIds);
        survivors.add(permanentId);
        return new DepartureBatch(intents, stage, destructionAnswers, replacementAnswers, awaitingAnswerIds,
                survivors, snapshots, ownerStackEntryId);
    }

    /** Records that the destruction replacement expected to keep {@code permanentId} no longer applies. */
    public DepartureBatch withoutSurvivor(UUID permanentId) {
        Set<UUID> survivors = new LinkedHashSet<>(survivorIds);
        survivors.remove(permanentId);
        return new DepartureBatch(intents, stage, destructionAnswers, replacementAnswers, awaitingAnswerIds,
                survivors, snapshots, ownerStackEntryId);
    }

    public DepartureBatch withSnapshot(DepartureSnapshot snapshot) {
        Map<UUID, DepartureSnapshot> updated = new LinkedHashMap<>(snapshots);
        updated.put(snapshot.permanentId(), snapshot);
        return new DepartureBatch(intents, stage, destructionAnswers, replacementAnswers, awaitingAnswerIds,
                survivorIds, updated, ownerStackEntryId);
    }

    /** Whether any of its intents is performed by a state-based action check (CR 704.3). */
    public boolean isStateBased() {
        return intents.stream().anyMatch(intent -> intent.cause().isStateBased());
    }

    /** Whether a question asked for this batch is still open. */
    public boolean isAwaitingAnswer() {
        return !awaitingAnswerIds.isEmpty();
    }

    /** Copies the mutable last-known permanents of the snapshots; everything else is immutable. */
    public DepartureBatch deepCopy() {
        Map<UUID, DepartureSnapshot> copiedSnapshots = new LinkedHashMap<>();
        snapshots.forEach((permanentId, snapshot) -> copiedSnapshots.put(permanentId, snapshot.deepCopy()));
        return new DepartureBatch(intents, stage, destructionAnswers, replacementAnswers, awaitingAnswerIds,
                survivorIds, copiedSnapshots, ownerStackEntryId);
    }

    private Set<UUID> answered(UUID permanentId) {
        Set<UUID> awaiting = new LinkedHashSet<>(awaitingAnswerIds);
        awaiting.remove(permanentId);
        return awaiting;
    }

    private static <V> Map<UUID, V> orderedCopy(Map<UUID, V> source) {
        Map<UUID, V> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(Objects.requireNonNull(key), Objects.requireNonNull(value)));
        return Collections.unmodifiableMap(copy);
    }
}
