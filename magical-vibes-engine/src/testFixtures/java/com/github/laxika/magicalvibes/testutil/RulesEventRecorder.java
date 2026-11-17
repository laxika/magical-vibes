package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.RulesObserver;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Watches one game ({@link GameData#rulesObserver}): records every rules event batch the trigger pipeline drains, for
 * tests that look at the events an action emits, and hands every triggered ability that reaches the stack, and every
 * floating effect that begins or ends, to the {@link TriggerLedger} once.
 */
public final class RulesEventRecorder implements RulesObserver {

    private final GameData gameData;
    private final Set<Object> ledgered = Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<RulesEventBatch> drained = new ArrayList<>();
    /** Batches with an id up to this one were recorded before the last {@link #clear}. */
    private long clearedThroughBatchId;

    private RulesEventRecorder(GameData gameData) {
        this.gameData = gameData;
    }

    /** Starts recording {@code gameData}, replacing any observer it had. */
    public static RulesEventRecorder attach(GameData gameData) {
        RulesEventRecorder recorder = new RulesEventRecorder(gameData);
        gameData.rulesObserver = recorder;
        return recorder;
    }

    @Override
    public void triggerQueued(Route route, Object item) {
        if (!(item instanceof StackEntry entry) || route == Route.MAY_ABILITY || route == Route.INTERACTION
                || !ledgered.add(item)) {
            return;
        }
        TriggerLedger.record(gameData, entry, route == Route.STACK_ADDITIONAL);
    }

    @Override
    public void floatingEffectBegan(FloatingContinuousEffect effect) {
        TriggerLedger.recordFloating(gameData, effect, true);
    }

    @Override
    public void floatingEffectEnded(FloatingContinuousEffect effect) {
        TriggerLedger.recordFloating(gameData, effect, false);
    }

    @Override
    public void eventsDrained(List<RulesEventBatch> batches) {
        drained.addAll(batches);
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
     * Forgets the batches recorded so far, so a test can look at what one action emits. Batches still pending stay
     * in the game; the recorder only stops reporting them.
     */
    public void clear() {
        for (RulesEventBatch batch : batches()) {
            clearedThroughBatchId = Math.max(clearedThroughBatchId, batch.id());
        }
        drained.clear();
    }
}
