package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Static effect that grants a creature subtype to permanents matching the given scope.
 * For equipment: "equipped creature is a [color] Zombie" adds the subtype.
 * For auras: "enchanted creature is a [type]" similarly.
 *
 * @param subtype    the creature subtype to grant
 * @param scope      which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, etc.)
 * @param overriding when true, replaces all existing creature subtypes instead of adding (e.g. "is a black Zombie")
 */
public record GrantSubtypeEffect(CardSubtype subtype, GrantScope scope, boolean overriding) implements CardEffect {

    public GrantSubtypeEffect(CardSubtype subtype, GrantScope scope) {
        this(subtype, scope, false);
    }
}
