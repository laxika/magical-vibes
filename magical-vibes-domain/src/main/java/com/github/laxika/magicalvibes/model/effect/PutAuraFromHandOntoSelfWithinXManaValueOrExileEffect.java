package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may put an Aura card with mana value X or less from your hand onto the battlefield attached to
 * this creature. If you don't, exile this creature." (Evershrike). The mana-value cap is read from the
 * ability's X (the stack entry's xValue); if no eligible Aura is put, the source creature is exiled.
 */
public record PutAuraFromHandOntoSelfWithinXManaValueOrExileEffect() implements CardEffect {
}
