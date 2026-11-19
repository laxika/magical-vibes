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
 * <p>Example: "You destroy four lands you control" →
 * {@code new PlayerDestroysPermanentsEffect(4, new PermanentIsLandPredicate(), Subjects.you())}
 *
 * @param count  number of permanents to destroy
 * @param filter which permanents are eligible
 * @param player who destroys (one player, the first the subject names); only {@code targetPlayer()} declares a
 *               (benign) player target
 */
public record PlayerDestroysPermanentsEffect(DynamicAmount count, PermanentPredicate filter,
        PlayerSubject player) implements CardEffect {

    /** Fixed count. */
    public PlayerDestroysPermanentsEffect(int count, PermanentPredicate filter, PlayerSubject player) {
        this(new Fixed(count), filter, player);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
