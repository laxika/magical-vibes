package com.github.laxika.magicalvibes.model.amount;

/**
 * "That creature's power": the power of the stack entry's triggering permanent as the ability resolves, its current
 * power while it is on the battlefield, else the value the collector recorded as the ability triggered, else its
 * departure snapshot (CR 608.2h). Counted as at least 0 unless {@code allowNegative}: "base power becomes equal to
 * that creature's power" copies a negative power (Belligerent Yearling).
 *
 * @param allowNegative whether a negative power is kept rather than counted as 0
 */
public record TriggeringPermanentPower(boolean allowNegative) implements DynamicAmount {

    public TriggeringPermanentPower() {
        this(false);
    }
}
