package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ULG", collectorNumber = "9")
public class HopeAndGlory extends Card {

    public HopeAndGlory() {
        target(TargetFilters.creature(), 2, 2)
                .addSpellEffect(new UntapPermanentsEffect(Subjects.allTargets()))
                .addSpellEffect(BoostEffect.target(1, 1));
    }
}
