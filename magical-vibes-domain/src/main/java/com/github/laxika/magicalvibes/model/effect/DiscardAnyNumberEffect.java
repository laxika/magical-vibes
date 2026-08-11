package com.github.laxika.magicalvibes.model.effect;

/**
 * Has the controller choose zero or more cards from their hand to discard. When {@code random} is
 * true, the controller chooses only the number and the cards are discarded at random. The handler
 * records the chosen count as the stack entry's event value so a following effect can use
 * "discarded this way" amounts.
 */
public record DiscardAnyNumberEffect(boolean random) implements CardEffect {

    /** Controller chooses which cards to discard. */
    public DiscardAnyNumberEffect() {
        this(false);
    }
}
