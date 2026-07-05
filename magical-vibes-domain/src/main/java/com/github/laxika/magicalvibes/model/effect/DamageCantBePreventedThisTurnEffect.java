package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, damage can't be prevented by any means. Sets the
 * {@code GameData.damageCantBePreventedThisTurn} flag (cleared at turn cleanup). Unlike the static
 * {@link DamageCantBePreventedEffect} (which relies on a permanent staying on the battlefield),
 * this is used by spells such as Impractical Joke.
 */
public record DamageCantBePreventedThisTurnEffect() implements CardEffect {
}
