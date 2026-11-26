package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "171")
public class SuddenSpinnerets extends Card {

    public SuddenSpinnerets() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(1, 3))
                .addSpellEffect(new PutCountersEffect(CounterType.REACH, 1))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
