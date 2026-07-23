package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Combat trigger: schedule the combat opponent (the creature this permanent blocks, or that becomes
 * blocked by this permanent) to receive {@code amount} counters of {@code counterType} at end of
 * combat. Greater Werewolf-style "At end of combat, put a -0/-2 counter on each creature blocking or
 * blocked by this creature." With {@code alsoTap=true}, those creatures are also tapped (Dread
 * Wight — paralyzation counters).
 * <p>
 * Placed on the {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BLOCK} slot (for the
 * "blocking" half, auto-targeting the blocked attacker) and on
 * {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_BECOMES_BLOCKED} with
 * {@link com.github.laxika.magicalvibes.model.TriggerMode#PER_BLOCKER} (for the "blocked by" half,
 * one trigger per blocker). The referenced creature is passed as the stack entry's target but the
 * trigger does not target (it can't fizzle). At resolution a delayed
 * {@link com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat} is queued.
 *
 * @param counterType the type of counter to place on the combat opponent
 * @param amount      how many counters to place
 * @param alsoTap     when true, also tap the combat opponent at end of combat
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
        return TargetSpec.harmful(TargetCategory.PERMANENT);
    }
}
