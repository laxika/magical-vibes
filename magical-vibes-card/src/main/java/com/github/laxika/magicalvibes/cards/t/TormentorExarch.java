package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "97")
public class TormentorExarch extends Card {

    public TormentorExarch() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+0 until end of turn",
                        new BoostTargetCreatureEffect(2, 0),
                        TargetFilters.creature()
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -0/-2 until end of turn",
                        new BoostTargetCreatureEffect(0, -2),
                        TargetFilters.creature()
                )
        )));
    }
}
