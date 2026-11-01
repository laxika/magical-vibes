package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.EffectSlot;

import java.util.Map;
import java.util.Set;

/**
 * The slots in shadow mode while their family moves from its slot collectors to the trigger pipeline. For their
 * abilities the pipeline reports what it would trigger, and {@link ShadowTriggerRecorder} reports every trigger
 * one path has and the other lacks. Each difference that is not fixed is recorded here and in
 * effect-slot-refactor.md's Findings:
 *
 * @param slots           the slots compared
 * @param nothingToDoDeclines effects whose old-path handler puts no ability on the stack when it would have
 *                        nothing to do (no linked object, no imprinted card, nothing to count). The ability
 *                        triggers anyway (CR 603.2: none of these has an intervening "if"), so a trigger only the
 *                        pipeline reports for one of them is no difference
 * @param knownDifferences tests ({@code Class#method}) whose difference is a limit of shadow mode itself, with the
 *                        reason; their comparison is skipped
 */
public record ShadowComparison(Set<EffectSlot> slots, Set<Class<?>> nothingToDoDeclines,
                               Map<String, String> knownDifferences) {

    /** Compares nothing: no family is in shadow mode. */
    public static final ShadowComparison NONE = new ShadowComparison(Set.of(), Set.of(), Map.of());

    /**
     * The comparison every card test runs (see {@code BaseCardTest}). None: effect-slot-refactor.md step 10b moved
     * the pilot, the source's own zone changes, to the trigger pipeline, and step 11 the creature-death slots.
     */
    public static final ShadowComparison CURRENT = NONE;

    public ShadowComparison {
        slots = Set.copyOf(slots);
        nothingToDoDeclines = Set.copyOf(nothingToDoDeclines);
        knownDifferences = Map.copyOf(knownDifferences);
    }
}
