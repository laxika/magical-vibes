package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the
 * associated card from their hand without paying its mana cost.
 * Used by Counterlash (one PendingMayAbility per eligible hand card).
 *
 * @param revealCardOnDecline whether declining the cast publicly identifies the card
 */
public record MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline) implements CardEffect {

    public MayCastFromHandWithoutPayingManaCostEffect() {
        this(true);
    }
}
