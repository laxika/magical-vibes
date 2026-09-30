package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player shuffles the creatures they own into their library, then players who shuffled a
 * nontoken creature reveal until they find a creature card and put it onto the battlefield.
 */
public record EachPlayerShufflesOwnedCreaturesIntoLibraryThenRevealsCreatureEffect()
        implements CardEffect {
}
