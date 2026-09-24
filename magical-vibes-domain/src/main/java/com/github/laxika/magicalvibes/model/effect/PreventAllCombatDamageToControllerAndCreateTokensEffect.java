package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents all combat damage that would be dealt to the controller this turn and creates one
 * token for each damage prevented.
 */
public record PreventAllCombatDamageToControllerAndCreateTokensEffect(CreateTokenEffect token)
        implements CardEffect {
}
