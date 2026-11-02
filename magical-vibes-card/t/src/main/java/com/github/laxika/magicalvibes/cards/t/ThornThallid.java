package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "80a")
@CardRegistration(set = "FEM", collectorNumber = "80b")
@CardRegistration(set = "FEM", collectorNumber = "80c")
@CardRegistration(set = "FEM", collectorNumber = "80d")
@CardRegistration(set = "FEM", collectorNumber = "165")
@CardRegistration(set = "FEM", collectorNumber = "166")
@CardRegistration(set = "FEM", collectorNumber = "168")
@CardRegistration(set = "ME1", collectorNumber = "135")
public class ThornThallid extends Card {

    public ThornThallid() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PutCountersOnSelfEffect(CounterType.FUNGUS));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.FUNGUS),
                        new DealDamageToAnyTargetEffect(1)
                ),
                "Remove three spore counters from Thorn Thallid: It deals 1 damage to any target."
        ));
    }
}
