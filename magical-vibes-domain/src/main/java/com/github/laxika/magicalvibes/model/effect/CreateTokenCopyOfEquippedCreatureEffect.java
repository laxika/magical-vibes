package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token that's a copy of the creature the source equipment is attached to.
 * The token copies all copiable characteristics per CR 707.2.
 *
 * @param removeLegendary if true, the token is not legendary (removes LEGENDARY supertype)
 * @param grantHaste      if true, the token gains haste
 */
public record CreateTokenCopyOfEquippedCreatureEffect(
        boolean removeLegendary,
        boolean grantHaste
) implements CardEffect {
}
