package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.StepKind;

import java.util.Objects;

/**
 * "That many", "that much", "equal to the number of cards [drawn | milled | discarded] this way", "equal to the damage
 * dealt this way": the amount the most recent {@code kind} step of the resolving entry recorded on its resolution
 * scratchpad ({@code StackEntry.getResolutionResults().outcome(kind).amount()}; what each kind counts is listed on
 * {@link StepKind}). 0 when no such step ran or it did nothing, and outside a resolution. The answer is read once, as
 * the reading effect is applied (CR 608.2h).
 */
public record AmountThisWay(StepKind kind) implements DynamicAmount {

    public AmountThisWay {
        Objects.requireNonNull(kind, "kind");
    }
}
