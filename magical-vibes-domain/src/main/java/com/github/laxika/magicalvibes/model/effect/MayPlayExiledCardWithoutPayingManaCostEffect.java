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
 */
public record MayPlayExiledCardWithoutPayingManaCostEffect(boolean exclusive) implements CardEffect {

    public MayPlayExiledCardWithoutPayingManaCostEffect() {
        this(false);
    }
}
