package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The shadow recorder: watches one game ({@link GameData#rulesObserver}) and records both sides of shadow mode.
 *
 * <ul>
 *   <li>The old path: every triggered ability a slot collector put toward the stack, through
 *       {@code enqueueTrigger}, a direct {@code stack.add}, {@code queueMayAbility} or a pending may ability, or a
 *       queued interaction. An item is recorded once, however many of those routes it takes (a stack entry that
 *       waited for a mana ability to finish is recorded when it was first queued).</li>
 *   <li>The new path's input: every rules event batch, those the engine drained and those still pending.</li>
 * </ul>
 *
 * <p>{@link #differences} compares the old path's triggers that a {@link ShadowComparison} covers with what its
 * matcher triggers for the recorded batches. Interactions carry no source or effects the recorder can read, so
 * they are kept for diagnosis only; a trigger that goes on the stack after its interaction is answered is
 * compared through its stack entry.
 */
public final class ShadowTriggerRecorder implements RulesObserver {

    /**
     * One triggered ability, as either path would put it on the stack. Effects compare by value, so a trigger
     * matches when both paths give the same source the same effects.
     *
     * @param sourceCardId the card whose ability it is, or {@code null} when the item names none
     */
    public record ShadowTrigger(UUID sourceCardId, List<CardEffect> effects) {
        public ShadowTrigger {
            effects = List.copyOf(effects);
        }
    }

    /** An old-path trigger as recorded: the route it took, the trigger, and the recorded item itself. */
    public record QueuedTrigger(Route route, ShadowTrigger trigger, Object item) {}

    /** Triggers only one path has; both lists are empty when the paths agree. */
    public record Differences(List<ShadowTrigger> onlyOldPath, List<ShadowTrigger> onlyNewPath) {
        public boolean isEmpty() {
            return onlyOldPath.isEmpty() && onlyNewPath.isEmpty();
        }
    }

    private final GameData gameData;
    private final List<QueuedTrigger> oldPath = new ArrayList<>();
    private final Set<Object> recordedItems = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<RulesEventBatch> drained = new ArrayList<>();
    private ShadowComparison comparison = ShadowComparison.CURRENT;
    /** Batches with an id up to this one were recorded before the last {@link #clear}. */
    private long clearedThroughBatchId;

    private ShadowTriggerRecorder(GameData gameData) {
        this.gameData = gameData;
    }

    /** Starts recording {@code gameData}, replacing any observer it had. */
    public static ShadowTriggerRecorder attach(GameData gameData) {
        ShadowTriggerRecorder recorder = new ShadowTriggerRecorder(gameData);
        gameData.rulesObserver = recorder;
        return recorder;
    }

    /** Compares with {@code comparison} instead of {@link ShadowComparison#CURRENT}. */
    public void setComparison(ShadowComparison comparison) {
        this.comparison = comparison;
    }

    @Override
    public void triggerQueued(Route route, Object item) {
        if (!recordedItems.add(item)) {
            return;
        }
        ShadowTrigger trigger = switch (item) {
            case StackEntry entry -> new ShadowTrigger(cardId(entry.getCard()), entry.getEffectsToResolve());
            case PendingMayAbility may -> new ShadowTrigger(cardId(may.sourceCard()), may.effects());
            default -> new ShadowTrigger(null, List.of());
        };
        oldPath.add(new QueuedTrigger(route, trigger, item));
    }

    @Override
    public void eventsDrained(List<RulesEventBatch> batches) {
        drained.addAll(batches);
    }

    /** Every old-path trigger recorded so far, in the order they were queued. */
    public List<QueuedTrigger> oldPath() {
        return List.copyOf(oldPath);
    }

    /** Every rules event batch so far: those drained, then those still pending in the game. */
    public List<RulesEventBatch> batches() {
        List<RulesEventBatch> batches = new ArrayList<>(drained);
        batches.addAll(gameData.rulesEvents.pending());
        batches.removeIf(batch -> batch.id() <= clearedThroughBatchId);
        return batches;
    }

    /** Every zone change event so far, in order. */
    public List<ZoneChangeEvent> zoneChanges() {
        List<ZoneChangeEvent> zoneChanges = new ArrayList<>();
        for (RulesEventBatch batch : batches()) {
            for (RulesEvent event : batch.events()) {
                if (event instanceof ZoneChangeEvent zoneChange) {
                    zoneChanges.add(zoneChange);
                }
            }
        }
        return zoneChanges;
    }

    /**
     * Forgets everything recorded so far, so a test can look at what one action records. Batches still pending
     * stay in the game for the trigger pipeline; the recorder only stops reporting them.
     */
    public void clear() {
        oldPath.clear();
        recordedItems.clear();
        for (RulesEventBatch batch : batches()) {
            clearedThroughBatchId = Math.max(clearedThroughBatchId, batch.id());
        }
        drained.clear();
    }

    /** The triggers only one path has, for the families the comparison covers. */
    public Differences differences() {
        List<ShadowTrigger> onlyOldPath = new ArrayList<>();
        for (QueuedTrigger queued : oldPath) {
            if (queued.trigger().sourceCardId() != null && comparison.compares(gameData, queued.trigger())) {
                onlyOldPath.add(queued.trigger());
            }
        }
        List<ShadowTrigger> onlyNewPath = new ArrayList<>();
        for (ShadowTrigger trigger : comparison.newPath(gameData, batches())) {
            if (!onlyOldPath.remove(trigger)) {
                onlyNewPath.add(trigger);
            }
        }
        return new Differences(onlyOldPath, onlyNewPath);
    }

    /** Fails when the two paths disagree (see {@link #differences}). */
    public void assertPathsAgree() {
        Differences differences = differences();
        if (!differences.isEmpty()) {
            throw new AssertionError("Shadow mode: the slot collectors and the trigger pipeline disagree. Only the"
                    + " old path triggered " + differences.onlyOldPath() + "; only the new path triggered "
                    + differences.onlyNewPath() + ". Fix the difference, or record it in effect-slot-refactor.md's"
                    + " Findings as an old-path bug.");
        }
    }

    private static UUID cardId(Card card) {
        return card == null ? null : card.getId();
    }
}
