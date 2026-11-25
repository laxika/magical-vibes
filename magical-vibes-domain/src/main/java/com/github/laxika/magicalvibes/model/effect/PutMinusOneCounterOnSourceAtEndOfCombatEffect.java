package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: "Whenever this creature attacks or blocks, put a -1/-1 counter on it at end of
 * combat." (e.g. Wicker Warcrawler). Resolution registers a delayed "at end of combat" ability carrying a
 * {@link PutCountersEffect#onSelf(com.github.laxika.magicalvibes.model.CounterType)}, so the counter lands after combat damage and the delayed trigger
 * uses the stack. Put on the Triggers.attacks() and/or Triggers.blocks() effect slot.
 */
public record PutMinusOneCounterOnSourceAtEndOfCombatEffect() implements CardEffect {
}
