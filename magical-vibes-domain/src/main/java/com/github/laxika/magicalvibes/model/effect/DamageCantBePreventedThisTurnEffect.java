package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, damage can't be prevented by any means. It lasts as a floating effect
 * until end of turn (CR 611.2a), which {@code GameData.damageCantBePreventedThisTurn()} reports. Unlike the static
 * {@link DamageCantBePreventedEffect} (which relies on a permanent staying on the battlefield),
 * this is used by spells such as Impractical Joke.
 */
public record DamageCantBePreventedThisTurnEffect() implements CardEffect {
}
