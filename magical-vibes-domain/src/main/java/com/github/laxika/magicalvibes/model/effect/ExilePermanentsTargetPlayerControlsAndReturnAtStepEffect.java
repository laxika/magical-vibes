package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles all permanents matching the given predicate that the targeted player controls,
 * then returns each at the beginning of the next {@code returnStep} under its owner's control.
 * The target player is stored in {@code targetId} on the stack entry.
 *
 * <p>Example: Sudden Disappearance uses {@code PermanentNotPredicate(PermanentIsLandPredicate())}
 * with {@code TurnStep.END_STEP} to exile all nonland permanents the target player controls.
 */
public record ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect(
        PermanentPredicate predicate,
        TurnStep returnStep,
        boolean returnTapped) implements CardEffect {

    public ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect(PermanentPredicate predicate) {
        this(predicate, TurnStep.END_STEP, false);
    }

    public ExilePermanentsTargetPlayerControlsAndReturnAtStepEffect(
            PermanentPredicate predicate, TurnStep returnStep) {
        this(predicate, returnStep, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
