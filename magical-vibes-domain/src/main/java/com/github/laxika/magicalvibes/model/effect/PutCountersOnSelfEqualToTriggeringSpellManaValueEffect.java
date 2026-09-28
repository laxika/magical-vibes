package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Puts counters on this permanent equal to the mana value of the spell that caused the trigger. */
public record PutCountersOnSelfEqualToTriggeringSpellManaValueEffect(CounterType counterType)
        implements TriggeringSpellManaValueEffect {
}
