package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Continuous effect that sets the base power and toughness of creatures in a subject to
 * dynamically evaluated amounts. A floating instance may use {@link Subjects#source()} to
 * represent a granted self ability that changes only the source's base power or toughness.
 */
public record SetBasePowerToughnessToAmountEffect(
        DynamicAmount power,
        DynamicAmount toughness,
        PermanentSubject subject,
        Duration duration
) implements CardEffect {

    /** For as long as the subject's form lasts: a target until end of turn, the source with no end. */
    public SetBasePowerToughnessToAmountEffect(DynamicAmount power, DynamicAmount toughness,
                                               PermanentSubject subject) {
        this(power, toughness, subject, null);
    }
}
