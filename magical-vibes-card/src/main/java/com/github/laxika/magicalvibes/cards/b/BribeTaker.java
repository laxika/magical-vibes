package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterForEachControlledCounterKindEffect;

@CardRegistration(set = "NCC", collectorNumber = "55")
@CardRegistration(set = "NCC", collectorNumber = "155")
public class BribeTaker extends Card {

    public BribeTaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterForEachControlledCounterKindEffect());
    }
}
