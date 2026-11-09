package com.github.laxika.magicalvibes.model.amount;

/**
 * "The number of times you chose a mode for that spell" (Riku of Many Paths): the modes chosen for the spell a cast
 * trigger was about, counting a mode chosen more than once each time (CR 700.2d), read from the spell on the stack,
 * else from the stack entry's snapshot of it. 0 when the entry has no triggering spell or the spell isn't modal.
 */
public record TriggeringSpellModeCount() implements DynamicAmount {
}
