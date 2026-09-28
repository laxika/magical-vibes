package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * The number of opponents dealt combat damage this turn by a source with the given name or
 * creature subtype. The source's name and subtypes are snapshotted when it deals the damage.
 */
public record OpponentsDealtCombatDamageBySourceNameOrSubtypeThisTurn(
        String sourceName, CardSubtype sourceSubtype) implements DynamicAmount {
}
