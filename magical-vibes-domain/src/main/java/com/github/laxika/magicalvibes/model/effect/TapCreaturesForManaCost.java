package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost that lets the controller tap any number of untapped creatures they control to reduce a
 * spell or activated ability's generic mana cost by one mana per creature.
 */
public record TapCreaturesForManaCost() implements CostEffect {

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return new PermanentIsCreaturePredicate();
    }
}
