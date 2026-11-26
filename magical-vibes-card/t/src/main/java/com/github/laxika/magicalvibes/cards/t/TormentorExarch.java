package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "97")
public class TormentorExarch extends Card {

    public TormentorExarch() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets +2/+0 until end of turn",
                        BoostEffect.target(2, 0),
                        TargetFilters.creature()
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature gets -0/-2 until end of turn",
                        BoostEffect.target(0, -2),
                        TargetFilters.creature()
                )
        ))));
    }
}
