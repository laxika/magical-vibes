package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all damage that would be dealt to you." (e.g. Glacial Chasm)
 *
 * <p>Applied per damage event to the controller of the permanent carrying this effect — both
 * noncombat damage (in {@code DamageSupport.dealDamageToPlayer}) and combat damage (in
 * {@code CombatDamageService.applyPlayerDamage}) via
 * {@code DamageSupport.applyControllerAllDamagePrevention}. Damage to the controller's permanents
 * is unaffected; contrast {@link PreventAllDamageToControllerAndExileFromGraveyardEffect}, which
 * carries a graveyard-exile rider.
 */
public record PreventAllDamageToControllerEffect() implements CardEffect {
}
