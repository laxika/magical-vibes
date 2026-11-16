package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the target permanent from combat, or the permanent the trigger event was about when
 * {@code triggeringPermanent}.
 * If the permanent is attacking, it stops attacking.
 * If it is blocking, it stops blocking.
 *
 * @param triggeringPermanent whether it acts on the stack entry's triggering permanent without targeting it
 *                            ("untap it and remove it from combat", Melee, CR 115.10a)
 */
public record RemoveTargetFromCombatEffect(boolean triggeringPermanent) implements CardEffect {

    public RemoveTargetFromCombatEffect() {
        this(false);
    }

    /** The permanent the trigger event was about is removed from combat. */
    public static RemoveTargetFromCombatEffect ofTriggeringPermanent() {
        return new RemoveTargetFromCombatEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return triggeringPermanent ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.creature());
    }
}
