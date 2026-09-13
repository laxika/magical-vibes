package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;

/**
 * Static restriction for a fixed set of card names: spells with those names can't be cast and
 * lands with those names can't be played.
 */
public record SpellsAndLandsWithSpecifiedNamesCantBePlayedEffect(Set<String> cardNames)
        implements CardNameRestrictionEffect {

    public SpellsAndLandsWithSpecifiedNamesCantBePlayedEffect {
        cardNames = Set.copyOf(cardNames);
    }

    @Override
    public Set<String> forbiddenSpellNames(Set<String> nontokenPermanentNames) {
        return cardNames;
    }

    @Override
    public Set<String> forbiddenNonbasicLandNames(Set<String> nontokenPermanentNames) {
        return cardNames;
    }

    @Override
    public Set<String> forbiddenLandNames(Set<String> nontokenPermanentNames) {
        return cardNames;
    }
}
