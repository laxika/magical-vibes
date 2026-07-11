package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A player destroys {@code count} permanents they control matching {@code filter}, choosing which.
 * Unlike {@link SacrificePermanentsEffect} this is <b>destruction</b> — regeneration and
 * indestructible apply. If the player controls at most {@code count} matching permanents, all of
 * them are destroyed with no choice; otherwise they pick which to destroy (multi-select) and the
 * chosen ones are destroyed simultaneously.
 *
 * <p>{@link DestroyRecipient} routes who destroys (controller / target player). {@code TARGET_PLAYER}
 * makes the effect target a player.
 *
 * <p>Example: "You destroy four lands you control" →
 * {@code new PlayerDestroysPermanentsEffect(4, new PermanentIsLandPredicate(), DestroyRecipient.CONTROLLER)}
 *
 * @param count     number of permanents to destroy
 * @param filter    which permanents are eligible
 * @param recipient who destroys
 */
public record PlayerDestroysPermanentsEffect(DynamicAmount count, PermanentPredicate filter,
        DestroyRecipient recipient) implements CardEffect {

    /** Fixed count. */
    public PlayerDestroysPermanentsEffect(int count, PermanentPredicate filter, DestroyRecipient recipient) {
        this(new Fixed(count), filter, recipient);
    }

    @Override
    public boolean canTargetPlayer() {
        return recipient == DestroyRecipient.TARGET_PLAYER;
    }
}
