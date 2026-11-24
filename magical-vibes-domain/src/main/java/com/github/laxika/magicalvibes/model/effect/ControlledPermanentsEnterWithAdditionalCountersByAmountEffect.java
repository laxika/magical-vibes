package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that gives matching permanents additional +1/+1 counters as they
 * enter (CR 614.1c).
 * <p>
 * By default the amount is evaluated from the source and its controller's current game state as
 * the permanent enters ("equal to the number of +1/+1 counters on this creature"). With
 * {@code readsEnteringPermanent} it is evaluated against the entering permanent instead, which is
 * then the amount's source: "for each mana from a Treasure spent to cast it"
 * ({@code TreasureManaSpentToCast}), "if its mana value is 4 or less"
 * ({@code FixedIfCondition(SourceMatchesPermanentPredicate(...))}). The entering permanent is read as
 * it would exist on the battlefield (CR 614.12), so a face-down one has mana value 0 (CR 202.3a,
 * 708.2a), and a permanent that wasn't cast had no mana spent to cast it.
 *
 * @param readsEnteringPermanent whether {@code count} reads the entering permanent rather than the source
 */
public record ControlledPermanentsEnterWithAdditionalCountersByAmountEffect(
        PermanentPredicate enteringPermanentPredicate,
        DynamicAmount count,
        boolean readsEnteringPermanent
) implements ControlledPermanentEntryReplacementEffect {

    public ControlledPermanentsEnterWithAdditionalCountersByAmountEffect(
            PermanentPredicate enteringPermanentPredicate, DynamicAmount count) {
        this(enteringPermanentPredicate, count, false);
    }

    /** "[Matching permanents] enter with an additional +1/+1 counter for each [amount of it]". */
    public static ControlledPermanentsEnterWithAdditionalCountersByAmountEffect ofEnteringPermanent(
            PermanentPredicate enteringPermanentPredicate, DynamicAmount count) {
        return new ControlledPermanentsEnterWithAdditionalCountersByAmountEffect(
                enteringPermanentPredicate, count, true);
    }

    @Override
    public int additionalCounterCount(Permanent enteringPermanent) {
        return 0;
    }

    @Override
    public DynamicAmount additionalCounterAmount() {
        return count;
    }

    @Override
    public boolean amountReadsEnteringPermanent() {
        return readsEnteringPermanent;
    }
}
