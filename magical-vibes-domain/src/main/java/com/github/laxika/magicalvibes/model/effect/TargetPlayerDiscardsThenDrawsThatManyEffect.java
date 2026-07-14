package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards {@code amount} cards, then draws as many cards as they discarded this way.
 * <p>
 * The discard is mandatory and player-chosen; if the target holds fewer than {@code amount} cards,
 * they discard their whole hand and draw only that many (the draw is tied to the number actually
 * discarded, not to {@code amount}). Used by Forget.
 *
 * @param amount number of cards the target player must discard
 */
public record TargetPlayerDiscardsThenDrawsThatManyEffect(int amount) implements CardEffect {
    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
