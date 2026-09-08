package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "148")
public class GalaGreeters extends Card {

    public GalaGreeters() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new ChooseModeNotYetChosenThisTurnEffect(List.of(
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
