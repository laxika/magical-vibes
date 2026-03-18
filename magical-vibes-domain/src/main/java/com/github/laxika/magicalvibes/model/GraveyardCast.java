package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * "You may cast this card from your graveyard." Uses the card's normal mana cost
 * and does not exile after resolution (unlike flashback). The card goes to the
 * graveyard normally if it dies or is countered, allowing repeated graveyard casts.
 */
public record GraveyardCast() implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }

    @Override
    public List<CastingCost> costs() {
        return List.of();
    }
}
