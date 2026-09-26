package com.github.laxika.magicalvibes.model.amount;

/**
 * The announced X value of the spell that caused a spell-cast trigger.
 *
 * <p>This is distinct from {@link XValue} in a {@code SpellCastTriggerEffect}: that legacy
 * trigger context uses {@code XValue} for effects whose X is the total mana spent to cast the
 * triggering spell.</p>
 */
public record TriggeringSpellXValue() implements DynamicAmount {
}
