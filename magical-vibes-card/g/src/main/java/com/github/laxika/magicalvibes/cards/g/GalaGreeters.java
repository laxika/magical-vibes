package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "148")
public class GalaGreeters extends Card {

    public GalaGreeters() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                ChooseOneEffect.notYetChosenThisTurn(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                "Put a +1/+1 counter on this creature.",
                                new PutCountersOnSourceEffect(1, 1, 1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Create a tapped Treasure token.",
                                CreateTokenEffect.ofTappedTreasureToken(1)),
                        new ChooseOneEffect.ChooseOneOption(
                                "You gain 2 life.",
                                new GainLifeEffect(2))
                )));
    }
}
