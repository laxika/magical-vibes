package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Static effect: spells and nonbasic lands can't have the same name as a nontoken permanent.
 */
public record SpellsAndNonbasicLandsWithNontokenPermanentNamesCantBePlayedEffect()
        implements CardNameRestrictionEffect {

    @Override
    public Set<String> forbiddenSpellNames(Set<String> nontokenPermanentNames) {
        return nontokenPermanentNames;
    }

    @Override
    public Set<String> forbiddenNonbasicLandNames(Set<String> nontokenPermanentNames) {
        return nontokenPermanentNames;
    }
}
