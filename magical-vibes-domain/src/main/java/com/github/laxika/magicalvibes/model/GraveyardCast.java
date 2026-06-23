package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * "You may cast this card from your graveyard." Uses the card's normal mana cost
 * and does not exile after resolution (unlike flashback). The card goes to the
 * graveyard normally if it dies or is countered, allowing repeated graveyard casts.
 *
 * @param controllerControlsPredicate optional condition requiring the casting
 *                                     player to control a matching permanent
 */
public record GraveyardCast(PermanentPredicate controllerControlsPredicate) implements CastingOption {

    public GraveyardCast() {
        this(null);
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
