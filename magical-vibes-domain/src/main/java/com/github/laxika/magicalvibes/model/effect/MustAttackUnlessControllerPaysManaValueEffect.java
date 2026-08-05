package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher must-attack: the active player may pay {X}, where X is the target creature's mana
 * value. If they don't pay, the creature attacks this turn if able and is destroyed at the
 * beginning of the next end step if it didn't attack.
 *
 * <p>The decision belongs to the active player (who controls the creature — the target filter
 * restricts it to a creature they have controlled continuously since the beginning of the turn).
 * Declining, or being unable to pay, applies both halves of the penalty, which are the same
 * effects Norritt applies unconditionally ({@link MustAttackThisTurnEffect} with
 * {@code forceAttackController = false} plus {@link DestroyTargetIfDidNotAttackAtEndStepEffect}).
 *
 * <p>Used by Arcum's Whistle.
 */
public record MustAttackUnlessControllerPaysManaValueEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
