package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a spell-cast trigger effect that reads the triggering spell's mana value as the stack entry's event value:
 * the collector sets the event value to the spell's mana value as the ability triggers, and the effect reads it as it
 * resolves. No effect is rebuilt with the mana value bound in.
 */
public interface TriggeringSpellManaValueEffect extends CardEffect {
}
