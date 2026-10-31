package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/**
 * The trigger-level properties of a triggered ability. "You may" isn't one of them: an optional
 * triggered ability goes on the stack whether or not its controller means to take the option, and
 * the choice is made as it resolves (CR 603.5), so it stays a {@code MayEffect}.
 *
 * @param interveningIf the intervening "if" condition, or {@code null} for none. It is checked when
 *                      the trigger event occurs, so the ability doesn't trigger while it is false,
 *                      and again as the ability resolves (CR 603.4).
 * @param limit         how often the ability may trigger
 * @param multiplicity  how many times one event triggers it
 */
public record TriggerOptions(Condition interveningIf, TriggerLimit limit, TriggerMultiplicity multiplicity) {

    /** No intervening "if" and no limit; triggers once for each matching occurrence. */
    public static final TriggerOptions DEFAULT = new TriggerOptions(null, TriggerLimit.NONE, TriggerMultiplicity.EACH);

    public TriggerOptions {
        Objects.requireNonNull(limit, "limit");
        Objects.requireNonNull(multiplicity, "multiplicity");
    }

    public TriggerOptions withInterveningIf(Condition condition) {
        return new TriggerOptions(Objects.requireNonNull(condition, "condition"), limit, multiplicity);
    }

    public TriggerOptions withLimit(TriggerLimit limit) {
        return new TriggerOptions(interveningIf, limit, multiplicity);
    }

    public TriggerOptions withMultiplicity(TriggerMultiplicity multiplicity) {
        return new TriggerOptions(interveningIf, limit, multiplicity);
    }
}
