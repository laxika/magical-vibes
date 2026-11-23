package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * "Note its power", "note that excess damage": a step that determines {@code amount} once, as it resolves (CR 608.2h),
 * and records it as the {@link com.github.laxika.magicalvibes.model.StepKind#NOTE} step's outcome (a negative amount
 * as 0, CR 107.1b). A later step reads it as {@code AmountThisWay.noted()}, and so does a delayed triggered ability the
 * same resolution makes ("you get a one-time boon with '…, where X is the noted number'", "at the beginning of your
 * next main phase, add that much {C}"), which keeps the number (CR 603.7a).
 *
 * @param amount what is noted
 */
public record NoteAmountEffect(DynamicAmount amount) implements CardEffect {

    public NoteAmountEffect {
        Objects.requireNonNull(amount, "amount");
    }
}
