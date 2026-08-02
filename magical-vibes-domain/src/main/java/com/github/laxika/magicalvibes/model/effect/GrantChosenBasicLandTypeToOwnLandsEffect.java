package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: lands controlled by the source's controller gain the source's chosen basic land
 * type in addition to their other types and its intrinsic mana ability.
 *
 * <p>The chosen type is read from the source permanent's {@code chosenSubtype} field. The type
 * grant is applied in the type layer; the matching mana ability is granted separately so additive
 * subtype grants preserve the land's existing abilities.
 */
public record GrantChosenBasicLandTypeToOwnLandsEffect() implements CardEffect {
}
