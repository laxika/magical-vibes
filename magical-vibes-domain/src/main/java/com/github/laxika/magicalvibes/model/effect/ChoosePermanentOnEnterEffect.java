package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "As this enters, choose another creature you control", "choose a permanent you don't control" (CR 614.1c): as the
 * permanent enters, its controller chooses among the permanents {@code candidates} names, and it keeps the choice as
 * its chosen permanent ({@code PermanentIsChosenPermanentPredicate}). With none to choose it enters without one.
 *
 * @param candidates among which permanents, read as a static ability of the entering permanent reads it
 * @param label      what is chosen, for the prompt ("another creature you control")
 */
public record ChoosePermanentOnEnterEffect(PermanentSubject candidates, String label) implements CardEffect {

    public ChoosePermanentOnEnterEffect {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(label, "label");
        if (candidates.declaresTarget() || candidates.needsStackEntry()) {
            throw new IllegalArgumentException("An as-enters choice has neither a target nor a stack entry: " + candidates);
        }
    }
}
