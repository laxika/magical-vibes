package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Marker effect used in the may ability system to indicate that the player may cast the
 * associated card from their hand without paying its mana cost.
 * Used by Counterlash (one PendingMayAbility per eligible hand card).
 *
 * @param revealCardOnDecline whether declining the cast publicly identifies the card
 */
public record MayCastFromHandWithoutPayingManaCostEffect(
        boolean revealCardOnDecline,
        UUID choiceGroupId,
        CardEffect declineEffect
) implements CardEffect {

    public MayCastFromHandWithoutPayingManaCostEffect() {
        this(true, null, null);
    }

    public MayCastFromHandWithoutPayingManaCostEffect(boolean revealCardOnDecline) {
        this(revealCardOnDecline, null, null);
    }
}
