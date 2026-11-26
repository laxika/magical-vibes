package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "178")
@CardRegistration(set = "WTH", collectorNumber = "102")
public class FitOfRage extends Card {

    public FitOfRage() {
        // Target creature gets +3/+3 and gains first strike until end of turn.
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(3, 3))
          .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
    }
}
