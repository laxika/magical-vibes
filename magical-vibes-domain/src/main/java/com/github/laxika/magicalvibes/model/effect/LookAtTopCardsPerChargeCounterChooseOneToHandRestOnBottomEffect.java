package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top X cards of your library, where X is the number of charge counters
 * on the source permanent. Put one of those cards into your hand and the rest on the
 * bottom of your library in any order.
 *
 * <p>The charge counter count is snapshotted into the stack entry's xValue before
 * sacrifice, so this works correctly with sacrifice-as-cost abilities.
 */
public record LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect() implements CardEffect {
}
