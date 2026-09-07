package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "50")
public class Timebender extends Card {

    public Timebender() {
        addMorph("{U}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Remove two time counters",
                        new AdjustTimeCountersOnTargetEffect(false)),
                new ChooseOneEffect.ChooseOneOption("Put two time counters",
                        new AdjustTimeCountersOnTargetEffect(true)))));
    }
}
