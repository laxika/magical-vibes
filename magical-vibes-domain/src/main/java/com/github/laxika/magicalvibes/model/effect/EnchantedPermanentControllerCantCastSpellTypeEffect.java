package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Static Aura effect: the controller of the enchanted permanent can't cast spells of the specified
 * types (Brand of Ill Omen: "Enchanted creature's controller can't cast creature spells").
 *
 * <p>Unlike {@link CantCastSpellTypeEffect}, the restricted player is derived from the enchanted
 * permanent's controller, not from the source's controller. Enforced in
 * {@code CastingPermissionService.getRestrictedSpellTypes}.
 */
public record EnchantedPermanentControllerCantCastSpellTypeEffect(Set<CardType> restrictedTypes)
        implements CardEffect {
}
