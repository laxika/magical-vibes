package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller may activate abilities of creatures they control as though those
 * creatures had haste. Unlike granting the {@code HASTE} keyword, this only lifts the summoning
 * sickness restriction on activated abilities (including tap abilities) — it does not let the
 * creatures attack. Used by Thousand-Year Elixir (LRW).
 */
public record ActivateCreatureAbilitiesAsThoughHasteEffect() implements CardEffect {
}
