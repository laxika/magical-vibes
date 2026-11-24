package com.github.laxika.magicalvibes.model.effect;

/**
 * Tapestry Warden's rule: "each creature you control with toughness greater than its power stations
 * permanents using its toughness rather than its power" (CR 702.184c). A rule of its source judged
 * as the creature stations, not an ability of the stationing creature.
 */
public record UseToughnessForStationEffect() implements CardEffect {
}
