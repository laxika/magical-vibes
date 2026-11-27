package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "69")
@CardRegistration(set = "ME2", collectorNumber = "161")
public class FeralThallid extends Card {

    public FeralThallid() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), PutCountersEffect.onSelf(CounterType.SPORE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.SPORE),
                        new RegenerateEffect()
                ),
                "Remove three spore counters from Feral Thallid: Regenerate Feral Thallid."
        ));
    }
}
