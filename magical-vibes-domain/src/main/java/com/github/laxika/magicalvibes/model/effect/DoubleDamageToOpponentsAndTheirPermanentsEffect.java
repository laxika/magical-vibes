package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a source would deal damage to an opponent or a permanent an opponent
 * controls, that source deals double that damage to that player or permanent instead."
 *
 * <p>Recipient-scoped, unlike the source-scoped doublers: it does not matter who controls the damage
 * source, only that the damage is dealt to an opponent of this permanent's controller or to a permanent
 * such an opponent controls. Contrast {@link DoubleDamageEffect} (global, Furnace of Rath),
 * {@link DoubleControllerDamageEffect} (sources controlled by this permanent's controller) and
 * {@link DoubleDamageToEnchantedPlayerEffect} (one enchanted player only).
 *
 * <p>Applied via {@code GameQueryService.getDamageToRecipientMultiplier}; multiple instances stack
 * multiplicatively. Used by Gisela, Blade of Goldnight together with
 * {@link PreventHalfDamageToControllerAndTheirPermanentsEffect}.
 */
public record DoubleDamageToOpponentsAndTheirPermanentsEffect() implements CardEffect {
}
