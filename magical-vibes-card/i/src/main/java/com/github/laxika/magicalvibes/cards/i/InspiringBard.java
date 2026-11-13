package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "189")
@CardRegistration(set = "HBG", collectorNumber = "214")
public class InspiringBard extends Card {

    public InspiringBard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bardic Inspiration — Target creature gets +2/+2 until end of turn",
                        new BoostTargetCreatureEffect(2, 2),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Song of Rest — You gain 3 life",
                        new GainLifeEffect(3))
        ))));
    }
}
