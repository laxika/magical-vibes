package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersAsCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "313")
public class TortureChamber extends Card {

    public TortureChamber() {
        // At the beginning of your upkeep, put a pain counter on Torture Chamber.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCountersOnSelfEffect(CounterType.PAIN));

        // At the beginning of your end step, Torture Chamber deals damage to you equal to the
        // number of pain counters on it.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(new CountersOnSource(CounterType.PAIN), DamageRecipient.CONTROLLER));

        // {1}, {T}, Remove all pain counters from Torture Chamber: It deals damage to target
        // creature equal to the number of pain counters removed this way.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new RemoveAllCountersAsCostEffect(CounterType.PAIN),
                        new DealDamageToTargetCreatureEffect(new XValue())),
                "{1}, {T}, Remove all pain counters from Torture Chamber: Torture Chamber deals damage to target creature equal to the number of pain counters removed this way.",
                TargetFilters.creature()
        ));
    }
}
