package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to you, prevent that damage. Exile a card from your
 * graveyard for each 1 damage prevented this way." (e.g. Immortal Coil)
 *
 * <p>Applied per damage event to the controller of the permanent carrying this effect — both
 * noncombat damage (in {@code DamageSupport.dealDamageToPlayer}) and combat damage (in
 * {@code CombatDamageService.applyPlayerDamage}) via {@code DamageSupport.applyImmortalCoilPrevention}.
 * All of the damage is prevented; up to that many cards are exiled from the controller's graveyard.
 */
public record PreventAllDamageToControllerAndExileFromGraveyardEffect() implements CardEffect {
}
