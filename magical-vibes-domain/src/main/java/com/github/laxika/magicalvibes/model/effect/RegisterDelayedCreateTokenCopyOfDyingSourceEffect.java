package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger descriptor for a delayed token copy whose counter amount is snapshotted from the
 * dying source. The death-trigger collector supplies the snapshot before putting this effect on
 * the stack; the normal effect handler registers the next-end-step token copy.
 *
 * @param minimumCounterCount minimum number of +1/+1 counters required for the trigger
 * @param counterDivisor divisor applied to the snapshotted counter count
 * @param initialPlusOnePlusOneCounters counter count to put on the delayed token, or zero before
 *                                      the death trigger supplies the snapshot
 */
public record RegisterDelayedCreateTokenCopyOfDyingSourceEffect(
        int minimumCounterCount,
        int counterDivisor,
        int initialPlusOnePlusOneCounters
) implements CardEffect {

    public RegisterDelayedCreateTokenCopyOfDyingSourceEffect(int minimumCounterCount, int counterDivisor) {
        this(minimumCounterCount, counterDivisor, 0);
    }

    public RegisterDelayedCreateTokenCopyOfDyingSourceEffect {
        if (minimumCounterCount < 0) {
            throw new IllegalArgumentException("minimumCounterCount must not be negative");
        }
        if (counterDivisor <= 0) {
            throw new IllegalArgumentException("counterDivisor must be positive");
        }
        if (initialPlusOnePlusOneCounters < 0) {
            throw new IllegalArgumentException("initialPlusOnePlusOneCounters must not be negative");
        }
    }

    public RegisterDelayedCreateTokenCopyOfDyingSourceEffect withInitialCounters(int counters) {
        return new RegisterDelayedCreateTokenCopyOfDyingSourceEffect(
                minimumCounterCount, counterDivisor, counters);
    }
}
