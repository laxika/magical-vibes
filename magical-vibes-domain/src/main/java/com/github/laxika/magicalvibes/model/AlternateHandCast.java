package com.github.laxika.magicalvibes.model;

import java.util.List;

/**
 * An alternate casting cost from hand that replaces the normal mana cost
 * (e.g. Demon of Death's Gate: pay 6 life and sacrifice 3 black creatures).
 */
public record AlternateHandCast(List<CastingCost> costs) implements CastingOption {

    @Override
    public Disposition disposition() {
        return Disposition.GRAVEYARD;
    }
}
