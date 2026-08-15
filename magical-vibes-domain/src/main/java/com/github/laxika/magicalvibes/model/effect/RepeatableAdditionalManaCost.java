package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * SPELL-slot additional cast cost: "As an additional cost to cast this spell, you may pay
 * [cost A] and/or [cost B] any number of times" (Primitive Justice). Each repetition is one
 * payment of one of {@code manaCosts}; the caster announces the chosen payments as the spell is
 * cast and the engine appends them to the spell's total mana cost, exactly like escalate.
 *
 * <p>The spell's announced X is the number of targets the payments buy — {@code 1 + repetitions} —
 * so pairing this with {@code Card.targetX} makes the target group scale with the payments made.
 * The individual chosen payments are snapshotted onto the stack entry so that a resolution-time
 * {@link com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount} can read how
 * many times a particular option was paid (Primitive Justice's "you gain 1 life for each
 * additional {1}{G} you paid").
 *
 * @param multikicker whether the repeated payments are multikicker payments and therefore count
 *                    as kicks for kicked-spell triggers
 */
public record RepeatableAdditionalManaCost(List<String> manaCosts, boolean multikicker) implements CostEffect {

    public RepeatableAdditionalManaCost(List<String> manaCosts) {
        this(manaCosts, false);
    }

    public static RepeatableAdditionalManaCost multikicker(List<String> manaCosts) {
        return new RepeatableAdditionalManaCost(manaCosts, true);
    }
}
