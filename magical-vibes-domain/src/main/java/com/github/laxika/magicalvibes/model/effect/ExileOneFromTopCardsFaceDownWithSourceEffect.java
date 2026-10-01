package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top cards of the controller's library, exile one face down tracked with the
 * source permanent, and put the rest on the bottom of that library in any order, or randomly when
 * {@code randomOrder} is true.
 */
public record ExileOneFromTopCardsFaceDownWithSourceEffect(int count, boolean randomOrder) implements CardEffect {

    public ExileOneFromTopCardsFaceDownWithSourceEffect(int count) {
        this(count, false);
    }
}
