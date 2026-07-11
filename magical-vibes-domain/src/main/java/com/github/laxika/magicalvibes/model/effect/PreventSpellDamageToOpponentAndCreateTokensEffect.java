package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a spell you control would deal damage to an opponent, prevent that damage.
 * Create tokens (per {@code token}) for each 1 damage prevented this way." (e.g. Hostility)
 *
 * <p>Applied per damage event in {@code DamageSupport.dealDamageToPlayer}: when the damage source is a
 * spell controlled by this permanent's controller and the damaged player is an opponent of that
 * controller, the damage is prevented and one token is created for each 1 damage prevented. The
 * {@code token} blueprint's own amount is ignored — the count equals the damage prevented.
 */
public record PreventSpellDamageToOpponentAndCreateTokensEffect(CreateTokenEffect token) implements CardEffect {
}
