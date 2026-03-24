package com.github.laxika.magicalvibes.model.effect;

/**
 * ETB replacement effect: "Raid — This creature enters with N +1/+1 counters
 * on it if you attacked this turn." (e.g. Rigging Runner)
 *
 * @param count the number of +1/+1 counters to add when raid is met
 */
public record EnterWithPlusOnePlusOneCountersIfRaidEffect(int count) implements ReplacementEffect {
}
