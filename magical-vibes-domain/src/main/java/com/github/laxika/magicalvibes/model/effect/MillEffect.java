package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * The {@link PlayerSubject} mills {@code count} cards. The amount is a {@link DynamicAmount}, so the same effect covers
 * a fixed count ("mills three cards"), an X value ({@code XValue}), a derived count such as charge counters on the
 * source ({@code CountersOnSource(CHARGE)}), or the number of cards in the milling player's hand
 * ({@code CardsInHand(TARGET_PLAYER)} for Dreamborn Muse: for a target, triggering or chosen player the amount is
 * evaluated for each milling player with that player as its target). Several players named by any other subject
 * ({@code eachPlayer()}, {@code eachOpponent()}) mill the same amount at once, as one event (CR 101.4).
 *
 * @param count  how many cards each player mills
 * @param player who mills; only {@code targetPlayer()} declares a (benign) player target
 */
public record MillEffect(DynamicAmount count, PlayerSubject player)
        implements CardEffect {

    /** Convenience constructor for a fixed mill count. */
    public MillEffect(int count, PlayerSubject player) {
        this(new Fixed(count), player);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
