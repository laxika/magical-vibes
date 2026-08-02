package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the source Aura's static effects be ignored until end of turn (Volrath's Curse:
 * "That creature's controller may sacrifice a permanent of their choice for that player to
 * ignore this effect until end of turn"). Resolution flags the source permanent
 * {@code auraEffectsIgnoredThisTurn}; {@code GameQueryService.hasAuraWithEffect} then skips
 * that Aura, which is the single lookup behind the enchanted-creature attack, block, and
 * activated-ability restrictions. The flag is cleared by the end-of-turn modifier reset.
 */
public record IgnoreSourceAuraEffectsUntilEndOfTurnEffect() implements CardEffect {
}
