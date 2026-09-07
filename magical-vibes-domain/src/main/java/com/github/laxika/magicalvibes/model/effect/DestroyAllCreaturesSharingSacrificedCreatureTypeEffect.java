package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys all creatures that share a creature type with the creature sacrificed as an additional
 * cost to cast the spell. The destruction does not allow regeneration.
 */
public record DestroyAllCreaturesSharingSacrificedCreatureTypeEffect() implements BoardWipeEffect {

    @Override
    public boolean sweepsBoard() {
        return true;
    }
}
