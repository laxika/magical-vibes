package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent chooses a nontoken creature to sacrifice. After all choices, the chosen creatures
 * are sacrificed together and the effect controller conjures a duplicate of each into their
 * graveyard.
 */
public record EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffect()
        implements CardEffect {
}
