package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * A static restriction whose forbidden card names are derived from battlefield permanents.
 */
public interface CardNameRestrictionEffect extends CardEffect {

    Set<String> forbiddenSpellNames(Set<String> nontokenPermanentNames);

    Set<String> forbiddenNonbasicLandNames(Set<String> nontokenPermanentNames);
}
