package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "189")
public class InspiringBard extends Card {

    public InspiringBard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bardic Inspiration — Target creature gets +2/+2 until end of turn",
                        new BoostTargetCreatureEffect(2, 2),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Song of Rest — You gain 3 life",
                        new GainLifeEffect(3))
        )));
    }
}
