package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses from among the lands they control a land of each basic land type, then
 * sacrifices all other lands.
 *
 * <p>A land with multiple basic land types may be chosen for more than one type. The effect is
 * non-targeting and makes the choices in active-player order before sacrificing anything.
 */
public record EachPlayerChoosesLandOfEachBasicTypeThenSacrificeRestEffect() implements CardEffect {
}
