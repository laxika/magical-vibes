package com.github.laxika.magicalvibes.model.amount;

/**
 * "The amount of mana spent to cast that spell" of the targeted spell (Mana Sculpt): the mana its controller spent to
 * cast it, as its stack entry recorded it as it became cast; 0 for a spell no longer on the stack and for a copy that
 * wasn't cast (CR 707.10). Read once, as the effect is applied (CR 608.2h), so a spell that is to be countered is read
 * before the counter step.
 */
public record TargetSpellManaSpent() implements DynamicAmount {
}
