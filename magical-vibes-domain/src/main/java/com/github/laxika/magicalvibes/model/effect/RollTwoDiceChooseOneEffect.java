package com.github.laxika.magicalvibes.model.effect;

/**
 * "Roll two d{@code sides} and choose one result": one roll of two dice (advantage replacements add dice to that roll
 * and ignore the lowest, CR 706.6), then the controller chooses one result, unasked when both are equal. The chosen
 * result becomes the resolving entry's X value and the other its event value, so the card's following steps read
 * "that result" as {@code XValue} and "the other result" as {@code EventValue} (Valiant, Wild, Arcane and Reckless
 * Endeavor).
 */
public record RollTwoDiceChooseOneEffect(int sides) implements CardEffect {

    public RollTwoDiceChooseOneEffect {
        if (sides < 2) {
            throw new IllegalArgumentException("A die must have at least two sides");
        }
    }
}
