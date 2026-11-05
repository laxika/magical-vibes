package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the card of a dying creature, the stack entry's triggering card, and creates a token copy of it when the
 * exile succeeds. The token-copy profile supplies the characteristic exceptions to the copy.
 */
public record ExileDyingCreatureAndCreateTokenCopyEffect(CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect)
        implements CardEffect {
}
