package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "75")
@CardRegistration(set = "SOI", collectorNumber = "87")
@CardRegistration(set = "W17", collectorNumber = "11")
public class SleepParalysis extends Card {

    public SleepParalysis() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(Subjects.attached()))
                .addStaticEffect(DoesntUntapEffect.enchanted());
    }
}
