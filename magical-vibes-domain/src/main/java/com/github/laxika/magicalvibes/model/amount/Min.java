package com.github.laxika.magicalvibes.model.amount;

import java.util.List;

/**
 * The minimum of the component amounts (the "cap"/clamp sibling of {@link Sum}). Each
 * component is evaluated independently and the smallest value wins, so "reduce toughness
 * by up to 4" = {@code Min(Fixed(4), Sum(TargetToughness, Fixed(-1)))}. Evaluates to 0
 * when empty.
 */
public record Min(List<DynamicAmount> amounts) implements DynamicAmount {

    public Min(DynamicAmount... amounts) {
        this(List.of(amounts));
    }
}
