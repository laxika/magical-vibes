package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: schedule the source permanent (the creature this effect is on) to receive a
 * -1/-1 counter at end of combat. "Whenever this creature attacks or blocks, put a -1/-1 counter
 * on it at end of combat." (e.g. Wicker Warcrawler). Delaying to end of combat keeps the creature
 * at full toughness during the combat damage step. At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.PutMinusOneCounterAtEndOfCombat} is queued for
 * the source permanent; it is drained in {@code CombatService.processEndOfCombatSourceCounters()}.
 * Put on the ON_ATTACK and/or ON_BLOCK effect slot.
 */
public record PutMinusOneCounterOnSourceAtEndOfCombatEffect() implements CardEffect {
}
