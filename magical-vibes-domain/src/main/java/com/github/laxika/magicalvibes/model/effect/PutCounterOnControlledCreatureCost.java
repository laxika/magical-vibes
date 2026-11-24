package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost: put {@code amount} counter(s) of the given type on a creature you control, as an additional cost to cast a
 * spell (Scarscale Ritual: "put a -1/-1 counter on a creature you control"), as part of an activation cost, or as a
 * forced cost. "Blight N" / "blight X" is the -1/-1 form ({@link #blight}, CR 701.68a); a creature that can't get the
 * counters can't pay it (CR 701.68b, 118.3). On a spell the creature is supplied via
 * {@code PlayCardRequest.sacrificePermanentId} (reuses the sacrifice-cost id field) and paid in
 * {@code SpellCastingService}; on an ability by the permanent-choice cost handler. The spell is unplayable if you
 * control no creature.
 *
 * @param counterType type of counter to place
 * @param amount      number of counters to place: a {@link Fixed} count, or {@link XValue} for the announced X of the
 *                    spell or ability (Soul Immolation, Blighted Nightmare)
 * @param optional    whether the caster may decline this cost
 */
public record PutCounterOnControlledCreatureCost(CounterType counterType, DynamicAmount amount, boolean optional)
        implements CostEffect {

    public PutCounterOnControlledCreatureCost(CounterType counterType, int count, boolean optional) {
        this(counterType, new Fixed(count), optional);
    }

    public PutCounterOnControlledCreatureCost(CounterType counterType, int count) {
        this(counterType, count, false);
    }

    /** "Blight N": put N -1/-1 counters on a creature you control. */
    public static PutCounterOnControlledCreatureCost blight(int count) {
        return new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, count);
    }

    /** "Blight X": put X -1/-1 counters on a creature you control, X announced with the spell or ability. */
    public static PutCounterOnControlledCreatureCost blightX() {
        return new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, new XValue(), false);
    }

    /**
     * The fixed number of counters, for the readers (forced costs) that only take a fixed count.
     *
     * @throws IllegalStateException when the amount is not fixed
     */
    public int count() {
        if (amount instanceof Fixed fixed) {
            return fixed.value();
        }
        throw new IllegalStateException("The counter count is " + amount + ", not fixed");
    }

    private static final PermanentPredicate CREATURE_FILTER = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return CREATURE_FILTER;
    }
}
