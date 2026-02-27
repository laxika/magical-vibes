package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static effect that grants a color to permanents matching the given scope.
 * For equipment: "equipped creature is a black [type]" adds the color.
 * For auras: "enchanted creature is [color]" similarly.
 *
 * @param color      the color to grant
 * @param scope      which permanents are affected (EQUIPPED_CREATURE, ENCHANTED_CREATURE, etc.)
 * @param overriding when true, replaces all existing colors instead of adding (e.g. "is a black Zombie")
 */
public record GrantColorEffect(CardColor color, GrantScope scope, boolean overriding) implements CardEffect {

    public GrantColorEffect(CardColor color, GrantScope scope) {
        this(color, scope, false);
    }
}
