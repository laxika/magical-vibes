package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that grants a subtype to permanents matching the given scope.
 * For equipment: "equipped creature is a [color] Zombie" adds the subtype.
 * For auras: "enchanted creature is a [type]" similarly.
 * For global enchantments: "each [filter] is an Equipment" with ALL_PERMANENTS scope.
 *
 * @param subtype    the subtype to grant
 * @param scope      which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, ALL_PERMANENTS, etc.)
 * @param overriding when true, replaces all existing creature subtypes instead of adding (e.g. "is a black Zombie")
 * @param filter     optional predicate to restrict which permanents are affected (used with ALL_PERMANENTS, OWN_PERMANENTS)
 */
public record GrantSubtypeEffect(CardSubtype subtype, GrantScope scope, boolean overriding,
                                  PermanentPredicate filter) implements CardEffect {

    public GrantSubtypeEffect(CardSubtype subtype, GrantScope scope) {
        this(subtype, scope, false, null);
    }

    public GrantSubtypeEffect(CardSubtype subtype, GrantScope scope, boolean overriding) {
        this(subtype, scope, overriding, null);
    }
}
