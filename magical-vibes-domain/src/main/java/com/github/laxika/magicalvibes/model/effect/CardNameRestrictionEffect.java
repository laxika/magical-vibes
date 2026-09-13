package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * A static restriction whose forbidden card names are derived from battlefield permanents.
 */
public interface CardNameRestrictionEffect extends CardEffect {

    Set<String> forbiddenSpellNames(Set<String> nontokenPermanentNames);

    Set<String> forbiddenNonbasicLandNames(Set<String> nontokenPermanentNames);

    /**
     * Returns names that can't be played as any land, including basic lands.
     * Restrictions that only apply to nonbasic lands leave this empty.
     */
    default Set<String> forbiddenLandNames(Set<String> nontokenPermanentNames) {
        return Set.of();
    }
}
