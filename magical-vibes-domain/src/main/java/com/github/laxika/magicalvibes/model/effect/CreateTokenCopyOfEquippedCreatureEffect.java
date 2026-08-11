package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Creates a token that's a copy of the creature the source equipment is attached to.
 * The token copies all copiable characteristics per CR 707.2.
 *
 * @param amount              number of copies to create
 * @param removeLegendary    if true, the token is not legendary (removes LEGENDARY supertype)
 * @param grantHaste         if true, the token gains haste
 * @param equipmentPermanentId the equipment to inspect; null uses the stack entry's source
 */
public record CreateTokenCopyOfEquippedCreatureEffect(
        int amount,
        boolean removeLegendary,
        boolean grantHaste,
        UUID equipmentPermanentId
) implements CardEffect {

    public CreateTokenCopyOfEquippedCreatureEffect(boolean removeLegendary, boolean grantHaste) {
        this(1, removeLegendary, grantHaste, null);
    }

    public CreateTokenCopyOfEquippedCreatureEffect(int amount, boolean removeLegendary, boolean grantHaste) {
        this(amount, removeLegendary, grantHaste, null);
    }
}
