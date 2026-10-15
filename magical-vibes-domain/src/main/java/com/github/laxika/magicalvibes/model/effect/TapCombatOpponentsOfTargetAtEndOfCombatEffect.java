package com.github.laxika.magicalvibes.model.effect;

/**
 * "At this turn's next end of combat, tap each creature that was blocked by one of those creatures
 * this turn and it doesn't untap during its controller's next untap step." The target group holds
 * the creatures whose combat opponents are checked when the delayed trigger resolves.
 * <p>
 * With {@code schedule} set (the card's form), resolution queues a
 * {@link com.github.laxika.magicalvibes.model.action.TapCombatOpponentsAtEndOfCombat} delayed
 * trigger for the targets. As the next end of combat step begins it goes on the stack carrying this
 * effect with {@code schedule} cleared and the targets as its triggering permanents; that trigger
 * taps and untap-locks the creatures they blocked.
 */
public record TapCombatOpponentsOfTargetAtEndOfCombatEffect(boolean schedule) implements CardEffect {

    public TapCombatOpponentsOfTargetAtEndOfCombatEffect() {
        this(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return schedule ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
