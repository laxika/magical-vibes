package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "2")
@CardRegistration(set = "TLE", collectorNumber = "151")
public class AcrobaticLeap extends Card {

    public AcrobaticLeap() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(1, 3))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.target()))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()));
    }
}
