package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureThenDrawEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "176")
public class MisterHydeMonsterWithin extends Card {

    public MisterHydeMonsterWithin() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on Mister Hyde.",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Remove a counter from a creature you control. If you do, draw a card.",
                        new RemoveCounterFromControlledCreatureThenDrawEffect())
        )));
    }
}
