package com.github.laxika.magicalvibes.model.amount;

/**
 * The announced X value of the spell that caused a spell-cast trigger.
 *
 * <p>This is distinct from {@link XValue} on a cast trigger's stack entry, which is the X the
 * spell-cast collector gives the ability (for example the total mana spent to cast the
 * triggering spell).</p>
 */
public record TriggeringSpellXValue() implements DynamicAmount {
}
