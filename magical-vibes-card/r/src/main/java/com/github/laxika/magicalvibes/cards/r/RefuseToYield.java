package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SNC", collectorNumber = "27")
public class RefuseToYield extends Card {

    public RefuseToYield() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 7))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
