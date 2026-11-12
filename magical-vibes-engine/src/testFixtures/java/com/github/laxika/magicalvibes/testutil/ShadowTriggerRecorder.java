package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
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
 *       queued interaction such as a trigger's target choice, with the slot whose collector was collecting. An
 *       item is recorded once, however many of those routes it takes.</li>
 *   <li>The new path: what the trigger pipeline reports it would trigger for the slots in shadow mode
 *       ({@link ShadowComparison}), and every rules event batch.</li>
 * </ul>
 *
 * <p>{@link #differences} compares the two for the shadowed slots. A trigger compares by its source card and the
 * classes of its effects, taken from the effect the collector dispatched: collectors bind event data into new
 * effect records ({@code …AwareEffect}s) and rewrite some effects into others before they queue them, which
 * step 15 of effect-slot-refactor.md removes. A trigger that waited for a target choice or a "may" question is
 * compared once, through that question: the stack entry it becomes once answered is put there outside any
 * collector, so it carries no slot and isn't compared.
 */
public final class ShadowTriggerRecorder implements RulesObserver {

    /**
     * One triggered ability, as either path would put it on the stack.
     *
     * @param sourceCardId  the card whose ability it is, or {@code null} when the item names none
     * @param sourceName    its name, for the failure message
     * @param effectClasses the classes of its effects, in order
     */
    public record ShadowTrigger(UUID sourceCardId, String sourceName, List<Class<?>> effectClasses) {

        public ShadowTrigger {
            effectClasses = List.copyOf(effectClasses);
        }

        static ShadowTrigger of(Card source, List<CardEffect> effects) {
            return new ShadowTrigger(source == null ? null : source.getId(), source == null ? null : source.getName(),
                    effects.stream().<Class<?>>map(Object::getClass).toList());
        }

        @Override
        public String toString() {
            return sourceName + effectClasses.stream().map(Class::getSimpleName).toList();
        }
    }

    /**
     * An old-path trigger as recorded.
     *
     * @param route   where the collector put it
     * @param slot    the slot whose collector put it there, or {@code null} outside any collector
     * @param trigger the trigger: the item's source with the effect the collector dispatched, which the trigger
     *                pipeline reports too, or with the item's own effects when no collector dispatched one
     * @param item    the recorded stack entry, pending may ability or interaction
     */
    public record QueuedTrigger(Route route, EffectSlot slot, ShadowTrigger trigger, Object item) {}

    /** Triggers only one path has; both lists are empty when the paths agree. */
    public record Differences(List<ShadowTrigger> onlyOldPath, List<ShadowTrigger> onlyNewPath) {
        public boolean isEmpty() {
            return onlyOldPath.isEmpty() && onlyNewPath.isEmpty();
        }
    }

    private final GameData gameData;
    private final List<QueuedTrigger> oldPath = new ArrayList<>();
    private final Set<Object> recordedItems = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<ShadowTrigger> newPath = new ArrayList<>();
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
    public Set<EffectSlot> shadowedSlots() {
        return comparison.slots();
    }

    @Override
    public void triggerQueued(Route route, Object item, GameData.CollectingTrigger collecting) {
        if (route == Route.STACK_ADDITIONAL) {
            if (item instanceof StackEntry additional && recordedItems.add(item)) {
                TriggerLedger.record(gameData, additional, true);
            }
            return;
        }
        if (!recordedItems.add(item)) {
            return;
        }
        if (item instanceof StackEntry entry && route != Route.MAY_ABILITY && route != Route.INTERACTION) {
            TriggerLedger.record(gameData, entry, false);
        }
        ShadowTrigger trigger = triggerOf(item);
        EffectSlot slot = collecting == null ? null : collecting.slot();
        if (collecting != null && collecting.effect() != null) {
            Card source = collecting.sourceCard();
            trigger = trigger.sourceCardId() != null
                    ? new ShadowTrigger(trigger.sourceCardId(), trigger.sourceName(),
                            List.of(collecting.effect().getClass()))
                    : ShadowTrigger.of(source, List.of(collecting.effect()));
        }
        oldPath.add(new QueuedTrigger(route, slot, trigger, item));
    }

    @Override
    public void wouldTrigger(EffectSlot slot, Card source, List<CardEffect> effects) {
        newPath.add(ShadowTrigger.of(source, effects));
    }

    @Override
    public void eventsDrained(List<RulesEventBatch> batches) {
        drained.addAll(batches);
    }

    /** Every old-path trigger recorded so far, in the order they were queued. */
    public List<QueuedTrigger> oldPath() {
        return List.copyOf(oldPath);
    }

    /** Every trigger the pipeline reported for the shadowed slots so far, in order. */
    public List<ShadowTrigger> newPath() {
        return List.copyOf(newPath);
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
     * stay in the game; the recorder only stops reporting them.
     */
    public void clear() {
        oldPath.clear();
        recordedItems.clear();
        newPath.clear();
        for (RulesEventBatch batch : batches()) {
            clearedThroughBatchId = Math.max(clearedThroughBatchId, batch.id());
        }
        drained.clear();
    }

    /** The triggers only one path has, for the slots in shadow mode. */
    public Differences differences() {
        List<ShadowTrigger> onlyOldPath = new ArrayList<>();
        for (QueuedTrigger queued : oldPath) {
            if (queued.trigger().sourceCardId() != null && queued.slot() != null
                    && comparison.slots().contains(queued.slot())) {
                onlyOldPath.add(queued.trigger());
            }
        }
        List<ShadowTrigger> onlyNewPath = new ArrayList<>();
        for (ShadowTrigger trigger : newPath) {
            if (!onlyOldPath.remove(trigger) && !isNothingToDoDecline(trigger)) {
                onlyNewPath.add(trigger);
            }
        }
        return new Differences(onlyOldPath, onlyNewPath);
    }

    private boolean isNothingToDoDecline(ShadowTrigger trigger) {
        return trigger.effectClasses().size() == 1
                && comparison.nothingToDoDeclines().contains(trigger.effectClasses().getFirst());
    }

    /**
     * Fails when the two paths disagree (see {@link #differences}), unless {@code testId} ({@code Class#method})
     * is one of the comparison's known differences.
     */
    public void assertPathsAgree(String testId) {
        if (!comparison.knownDifferences().containsKey(testId)) {
            assertPathsAgree();
        }
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

    /**
     * The trigger an item stands for: a stack entry's card and effects, or the first card and the first list of
     * effects among a record's components (a pending may ability, a trigger's target choice).
     */
    private static ShadowTrigger triggerOf(Object item) {
        if (item instanceof StackEntry entry) {
            return ShadowTrigger.of(entry.getCard(), entry.getEffectsToResolve());
        }
        if (!item.getClass().isRecord()) {
            return ShadowTrigger.of(null, List.of());
        }
        Card source = null;
        List<CardEffect> effects = null;
        try {
            for (RecordComponent component : item.getClass().getRecordComponents()) {
                if (source == null && component.getType() == Card.class) {
                    source = (Card) component.getAccessor().invoke(item);
                } else if (effects == null && isEffectList(component)) {
                    @SuppressWarnings("unchecked")
                    List<CardEffect> list = (List<CardEffect>) component.getAccessor().invoke(item);
                    effects = list;
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not read " + item.getClass().getSimpleName(), e);
        }
        return effects == null ? ShadowTrigger.of(null, List.of()) : ShadowTrigger.of(source, effects);
    }

    private static boolean isEffectList(RecordComponent component) {
        return component.getType() == List.class
                && component.getGenericType() instanceof ParameterizedType type
                && type.getActualTypeArguments()[0] == CardEffect.class;
    }
}
