package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "57")
@CardRegistration(set = "HA4", collectorNumber = "8")
public class AmmitEternal extends Card {

    public AmmitEternal() {
        // Afflict 3 — whenever this creature becomes blocked, the defending player loses 3 life
        // (once per becoming blocked, not per blocker). Heads-up, so the sole opponent is the defender.
        addTrigger(Triggers.becomesBlocked(), new LoseLifeEffect(3, Subjects.eachOpponent()));
        // Whenever an opponent casts a spell, put a -1/-1 counter on this creature (mandatory —
        // the source is bound by the dedicated Triggers.opponentCastsSpell() collector).
        addTrigger(Triggers.opponentCastsSpell(), PutCountersEffect.onSelf(CounterType.MINUS_ONE_MINUS_ONE));
        // Whenever this creature deals combat damage to a player, remove all -1/-1 counters from it.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RemoveAllCountersEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
