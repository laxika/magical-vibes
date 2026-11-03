package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/**
 * The trigger-level properties of a triggered ability. "You may" isn't one of them: an optional
 * triggered ability goes on the stack whether or not its controller means to take the option, and
 * the choice is made as it resolves (CR 603.5), so it stays a {@code MayEffect}.
 *
 * @param interveningIf  the intervening "if" condition, or {@code null} for none. It is checked when
 *                       the trigger event occurs, so the ability doesn't trigger while it is false,
 *                       and again as the ability resolves (CR 603.4).
 * @param limit          how often the ability may trigger
 * @param multiplicity   how many times one event triggers it
 * @param eventCondition a condition the trigger event itself requires, or {@code null} for none: "whenever
 *                       this creature attacks <b>while</b> you control a Dinosaur", "attacks alone", "an opponent
 *                       draws a card <b>during their turn</b>", "when the fourth plan counter is put on this", or
 *                       the condition under which the ability exists at all ("Max speed —", a station, level or
 *                       Spaceship threshold, an unlocked door, a chosen mode). An event that doesn't meet it isn't
 *                       the ability's trigger event (CR 603.2), so it is checked only as the event occurs, never
 *                       again as the ability resolves.
 */
public record TriggerOptions(Condition interveningIf, TriggerLimit limit, TriggerMultiplicity multiplicity,
                             Condition eventCondition) {

    /** No intervening "if" and no limit; triggers once for each matching occurrence. */
    public static final TriggerOptions DEFAULT = new TriggerOptions(null, TriggerLimit.NONE, TriggerMultiplicity.EACH);

    /** No intervening "if" and no limit; triggers once for an event however many of its occurrences match. */
    public static final TriggerOptions ONE_OR_MORE = DEFAULT.withMultiplicity(TriggerMultiplicity.ONE_OR_MORE);

    /** No intervening "if" and no limit; triggers once for each creature blocking the attacking creature. */
    public static final TriggerOptions EACH_BLOCKER = DEFAULT.withMultiplicity(TriggerMultiplicity.EACH_BLOCKER);

    /** No intervening "if" and no limit; triggers once for each source that deals damage in the event. */
    public static final TriggerOptions EACH_SOURCE = DEFAULT.withMultiplicity(TriggerMultiplicity.EACH_SOURCE);

    public TriggerOptions {
        Objects.requireNonNull(limit, "limit");
        Objects.requireNonNull(multiplicity, "multiplicity");
    }

    /** Options without an event condition. */
    public TriggerOptions(Condition interveningIf, TriggerLimit limit, TriggerMultiplicity multiplicity) {
        this(interveningIf, limit, multiplicity, null);
    }

    public TriggerOptions withInterveningIf(Condition condition) {
        return new TriggerOptions(Objects.requireNonNull(condition, "condition"), limit, multiplicity, eventCondition);
    }

    public TriggerOptions withLimit(TriggerLimit limit) {
        return new TriggerOptions(interveningIf, limit, multiplicity, eventCondition);
    }

    public TriggerOptions withMultiplicity(TriggerMultiplicity multiplicity) {
        return new TriggerOptions(interveningIf, limit, multiplicity, eventCondition);
    }

    public TriggerOptions withEventCondition(Condition condition) {
        return new TriggerOptions(interveningIf, limit, multiplicity, Objects.requireNonNull(condition, "condition"));
    }
}
