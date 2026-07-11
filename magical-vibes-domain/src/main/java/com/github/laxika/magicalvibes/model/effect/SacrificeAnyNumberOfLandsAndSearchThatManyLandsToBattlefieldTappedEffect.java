package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifice any number of lands. Search your library for up to that many land cards, put them
 * onto the battlefield tapped, then shuffle.
 *
 * <p>The controller chooses which (and how many) of the lands they control to sacrifice via a
 * {@link com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.SacrificeLandsSearchLandsToBattlefieldTapped}
 * multi-permanent choice; the number of land cards they may then search for equals the number
 * of lands actually sacrificed. Multi-pick search may fail to find. Used by Scapeshift.
 */
public record SacrificeAnyNumberOfLandsAndSearchThatManyLandsToBattlefieldTappedEffect() implements CardEffect {
}
