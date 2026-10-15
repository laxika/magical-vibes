package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: "Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of
 * combat." (e.g. Wicker Warcrawler). Resolution creates a
 * {@link com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger} carrying a
 * {@link PutCountersOnSelfEffect}, so the counter lands after combat damage and the delayed trigger
 * uses the stack. Put on the ON_ATTACK and/or ON_BLOCK effect slot.
 */
public record PutMinusOneCounterOnSourceAtEndOfCombatEffect() implements CardEffect {
}
