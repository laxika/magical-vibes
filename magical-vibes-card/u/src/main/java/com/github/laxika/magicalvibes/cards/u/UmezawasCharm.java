package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "111")
public class UmezawasCharm extends Card {

    public UmezawasCharm() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+2 until end of turn",
                        new BoostTargetCreatureEffect(2, 2),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -1/-1 until end of turn",
                        new BoostTargetCreatureEffect(-1, -1),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "You gain 2 life",
                        new GainLifeEffect(2))
        )));
    }
}
