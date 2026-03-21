package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * "You may cast this card from exile." Uses the card's normal mana cost
 * and does not have any special disposition — the card goes to the
 * graveyard normally if it dies or is countered.
 */
public record ExileCast() implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
