package com.github.laxika.magicalvibes.model.effect;

/**
 * Internal marker effect used in a {@code PendingMayAbility} to route a "you may play the exiled
 * card without paying its mana cost" choice. Not placed on cards directly — queued by
 * {@code CounterSupport} for {@link ReplaceControlledCounterWithExileAndPlayEffect} (Guile) and by
 * {@link MayCastCardsExiledWithSourceEffect} (Spell Queller), then handled by the may-ability
 * dispatch. If declined, the card stays exiled.
 *
 * <p>{@code exclusive} marks an offer that belongs to a "cast <em>a</em> spell from among those
 * cards" effect ({@link MayCastCardExiledWithSourceEffect}, Shell of the Last Kappa): one offer is
 * queued per eligible exiled card, and accepting any of them withdraws the remaining exclusive
 * offers so only a single spell is cast.</p>
 *
 * <p>{@code returnSourceToHand} makes the permanent that offered the cast return to its owner's hand
 * as part of the same resolution once the spell is cast ("If you do, return this Vehicle to its
 * owner's hand", Smuggler's Buggy).</p>
 */
public record MayPlayExiledCardWithoutPayingManaCostEffect(boolean exclusive, boolean grantHaste,
                                                           boolean returnSourceToHand) implements CardEffect {

    public MayPlayExiledCardWithoutPayingManaCostEffect(boolean exclusive, boolean grantHaste) {
        this(exclusive, grantHaste, false);
    }

    public MayPlayExiledCardWithoutPayingManaCostEffect(boolean exclusive) {
        this(exclusive, false, false);
    }

    public MayPlayExiledCardWithoutPayingManaCostEffect() {
        this(false, false, false);
    }
}
