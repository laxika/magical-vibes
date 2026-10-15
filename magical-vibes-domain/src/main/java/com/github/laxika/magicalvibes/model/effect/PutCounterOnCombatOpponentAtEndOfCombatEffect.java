package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Greater Werewolf-style "At end of combat, put a -0/-2 counter on each creature blocking or blocked
 * by this creature." With {@code alsoTap=true}, those creatures are also tapped (Dread Wight —
 * paralyzation counters). Put on
 * {@link com.github.laxika.magicalvibes.model.EffectSlot#END_OF_COMBAT_TRIGGERED}: the trigger goes on
 * the stack as the end of combat step begins and reads the creatures still blocking or blocked by
 * the source as it resolves.
 *
 * @param counterType the type of counter to place on each combat opponent
 * @param amount      how many counters to place
 * @param alsoTap     when true, also tap each combat opponent
 */
public record PutCounterOnCombatOpponentAtEndOfCombatEffect(
        CounterType counterType,
        int amount,
        boolean alsoTap
) implements CardEffect {

    /** Counter only (Greater Werewolf). */
    public PutCounterOnCombatOpponentAtEndOfCombatEffect(CounterType counterType, int amount) {
        this(counterType, amount, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }
}
