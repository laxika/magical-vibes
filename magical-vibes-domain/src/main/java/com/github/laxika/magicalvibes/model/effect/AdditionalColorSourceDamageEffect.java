package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static replacement effect: if another source of the given {@code color} controlled by this
 * permanent's controller would deal damage to a permanent or player, it deals that much damage
 * plus {@code amount} instead.
 *
 * <p>Unlike {@link AdditionalControllerDamageEffect} this covers every damage source of the
 * matching color — spells, abilities and combat damage alike — and it never boosts the permanent
 * carrying the effect itself ("another"). Two copies do boost each other.
 *
 * <p>Multiple instances stack additively. Only applies when the source would deal at least 1
 * damage. Queried by {@code GameQueryService.getColorSourcePermanentDamageBonus} for the damage
 * pool's source-bonus option, which the damaged player or permanent controller orders against the
 * other replacement effects.
 *
 * <p>Example: Embermaw Hellion — {@code new AdditionalColorSourceDamageEffect(1, CardColor.RED)}.
 */
public record AdditionalColorSourceDamageEffect(int amount, CardColor color) implements CardEffect {
}
