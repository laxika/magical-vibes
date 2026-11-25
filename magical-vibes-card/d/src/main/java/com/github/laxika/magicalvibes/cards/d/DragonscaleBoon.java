package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "131")
public class DragonscaleBoon extends Card {

    public DragonscaleBoon() {
        target(TargetFilters.creature())
                .addSpellEffect(new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, 2))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
