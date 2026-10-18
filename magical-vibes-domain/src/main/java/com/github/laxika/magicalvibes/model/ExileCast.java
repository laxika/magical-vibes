package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * "You may cast this card from exile." Uses the card's normal mana cost
 * and does not have any special disposition — the card goes to the
 * graveyard normally if it dies or is countered.
 *
 * @param onlyIfExiledByOwnHandAbility whether the permission only applies to a copy of the card that its own
 *                                     "Exile this card from your hand" ability exiled (Shattered Seraph), which
 *                                     is recorded as the exile entry's ability link
 */
public record ExileCast(boolean onlyIfExiledByOwnHandAbility) implements CastingOption {

    /** Ability link recorded on a card exiled by its own "Exile this card from your hand" ability. */
    public static final String OWN_HAND_ABILITY_LINK = "own-hand-ability";

    public ExileCast() {
        this(false);
    }

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
