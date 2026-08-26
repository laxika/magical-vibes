package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that grants a color to permanents matching the given scope.
 * For equipment: "equipped creature is a black [type]" adds the color.
 * For auras: "enchanted creature is [color]" similarly.
 * With {@link GrantScope#TARGET}, it can also be used by a resolving ability to permanently
 * set the target's colors when {@code overriding} is true.
 *
 * @param color      the color to grant
 * @param scope      which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, etc.)
 * @param overriding when true, replaces all existing colors instead of adding (e.g. "is a black Zombie")
 * @param filter     optional predicate to restrict which permanents are affected
 */
public record GrantColorEffect(CardColor color, GrantScope scope, boolean overriding,
                               PermanentPredicate filter) implements CardEffect {

    public GrantColorEffect(CardColor color, GrantScope scope) {
        this(color, scope, false, null);
    }

    public GrantColorEffect(CardColor color, GrantScope scope, boolean overriding) {
        this(color, scope, overriding, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.NONE;
    }
}
