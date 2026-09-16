package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of distinct effective creature types among creatures controlled by the amount's
 * controller. Changeling creatures contribute every creature type.
 */
public record CreatureTypesAmongControlledCreatures() implements DynamicAmount {
}
