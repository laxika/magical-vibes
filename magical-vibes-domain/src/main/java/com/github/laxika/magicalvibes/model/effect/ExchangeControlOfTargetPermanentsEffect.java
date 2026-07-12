package com.github.laxika.magicalvibes.model.effect;

/**
 * Exchanges control of the two permanents stored in {@code StackEntry.targetIds}: the first target
 * (a nonland permanent the ability's controller controls) and the second target (a nonland permanent
 * an opponent controls with equal or lesser mana value).
 *
 * <p>Used by Puca's Mischief's upkeep trigger. Target selection is mandatory at trigger time (the
 * pair must satisfy the mana-value restriction as a set); the "you may" is honoured at resolution by
 * wrapping this effect in a {@link MayEffect} (see Axis of Mortality for the same resolution-time
 * pattern). At resolution the exchange only happens if both targets are still legal (CR 701.10).
 */
public record ExchangeControlOfTargetPermanentsEffect() implements CardEffect {
}
