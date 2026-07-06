package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Forces the targeted player (from the stack entry's targetId) to discard cards.
 * The targeted player chooses which cards to discard.
 *
 * <p>The amount is a {@link DynamicAmount}, so the same effect covers a fixed count
 * ("discards two cards") or a derived count such as charge counters on the source
 * ({@code CountersOnSource(CHARGE)} — "a card for each charge counter").
 *
 * @param amount number of cards to discard
 */
public record TargetPlayerDiscardsEffect(DynamicAmount amount) implements CardEffect {

    /** Convenience constructor for a fixed discard count. */
    public TargetPlayerDiscardsEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
