package com.github.laxika.magicalvibes.model.effect;

/**
 * Devour N (CR 702.82): "As this creature enters, you may sacrifice any number of creatures.
 * It enters with {@code multiplier} times that many +1/+1 counters on it."
 * <p>
 * An as-enters replacement effect (MTG rule 614): placed in {@code EffectSlot.ON_ENTER_BATTLEFIELD}
 * and handled during {@code BattlefieldEntryService.handleCreatureEnteredBattlefield} before ETB
 * triggers fire. The controller chooses which of their other creatures to sacrifice; the entering
 * creature receives {@code multiplier * sacrificedCount} +1/+1 counters and records the number of
 * creatures devoured on the permanent (read by {@code CreaturesDevoured}).
 */
public record DevourEffect(int multiplier) implements ReplacementEffect {
}
