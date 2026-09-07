package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AdjustTimeCountersOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "109")
public class Timecrafting extends Card {

    public Timecrafting() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Remove X time counters from target permanent or suspended card",
                        new AdjustTimeCountersOnTargetEffect(false, new XValue())),
                new ChooseOneEffect.ChooseOneOption(
                        "Put X time counters on target permanent with a time counter on it or suspended card",
                        new AdjustTimeCountersOnTargetEffect(true, new XValue()))
        )));
    }
}
