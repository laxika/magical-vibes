package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOS", collectorNumber = "64")
public class Procrastinate extends Card {

    public Procrastinate() {
        target(TargetFilters.creature())
                .addSpellEffect(new TapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new PutCountersEffect(
                        CounterType.STUN,
                        new Scaled(new XValue(), 2)
                ));
    }
}
