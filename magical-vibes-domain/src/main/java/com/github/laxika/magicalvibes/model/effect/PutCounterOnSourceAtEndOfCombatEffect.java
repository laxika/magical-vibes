package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Combat trigger: schedule the source permanent to receive {@code amount} counters of
 * {@code counterType} at end of combat, optionally creating {@code tokenForController} for its
 * controller at the same time. "At end of combat, if this creature attacked or blocked this combat,
 * put a -0/-1 counter on this creature and create a 0/1 white Deserter creature token."
 * (Kjeldoran Home Guard). The generic sibling of
 * {@link PutMinusOneCounterOnSourceAtEndOfCombatEffect}, which is hardwired to -1/-1 counters.
 * <p>
 * At resolution a {@link com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat}
 * is queued for the source; it is drained in
 * {@code CombatService.processEndOfCombatOpponentCounters()}. Nothing happens if the source already
 * left the battlefield — matching the intervening-if trigger, which is gone with the permanent.
 * Put on the ON_ATTACK and/or ON_BLOCK effect slot.
 */
public record PutCounterOnSourceAtEndOfCombatEffect(
        CounterType counterType,
        int amount,
        CreateTokenEffect tokenForController
) implements CardEffect {

    public PutCounterOnSourceAtEndOfCombatEffect(CounterType counterType, int amount) {
        this(counterType, amount, null);
    }
}
