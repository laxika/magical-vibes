package com.github.laxika.magicalvibes.model.effect;

/**
 * For each attacking creature, its owner chooses whether to put it on top or bottom of their
 * library. The resolution-time choices are handled through the shared multi-permanent choice
 * interaction: the owner selects the creatures going to the top and the rest go to the bottom.
 */
public record PutAttackingCreaturesOnTopOrBottomOfLibraryEffect() implements CardEffect {
}
