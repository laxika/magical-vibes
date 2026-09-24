package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: prevent all combat damage that would be dealt to the controller this turn,
 * creating one token per damage prevented.
 */
public record PreventAllCombatDamageToControllerAndCreateTokensEffect(CreateTokenEffect token)
        implements CardEffect {
}
