package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;

import java.util.List;

/**
 * What shadow mode compares while an event family moves from its slot collectors to the trigger pipeline:
 * which of the old path's triggers belong to the families under comparison, and which triggers the new path's
 * matcher puts on the stack for the same rules events. {@link ShadowTriggerRecorder} reports every trigger one
 * path has and the other lacks.
 */
public interface ShadowComparison {

    /** Compares nothing: no event family is in shadow mode. */
    ShadowComparison NONE = new ShadowComparison() {
        @Override
        public boolean compares(GameData gameData, ShadowTriggerRecorder.ShadowTrigger oldPathTrigger) {
            return false;
        }

        @Override
        public List<ShadowTriggerRecorder.ShadowTrigger> newPath(GameData gameData, List<RulesEventBatch> batches) {
            return List.of();
        }
    };

    /**
     * The comparison every card test runs (see {@code BaseCardTest}). Each step that puts a family into shadow
     * mode widens it; no family is in shadow mode yet.
     */
    ShadowComparison CURRENT = NONE;

    /** Whether {@code oldPathTrigger}, which a slot collector queued, belongs to a family under comparison. */
    boolean compares(GameData gameData, ShadowTriggerRecorder.ShadowTrigger oldPathTrigger);

    /** The triggers the trigger pipeline puts toward the stack for {@code batches}, the game's rules events. */
    List<ShadowTriggerRecorder.ShadowTrigger> newPath(GameData gameData, List<RulesEventBatch> batches);
}
