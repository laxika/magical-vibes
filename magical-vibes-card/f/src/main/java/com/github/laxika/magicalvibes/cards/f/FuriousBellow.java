package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "126")
public class FuriousBellow extends Card {

    public FuriousBellow() {
        // Target creature gets +3/+0 and gains first strike until end of turn. Scry 1.
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()))
                .addSpellEffect(new ScryEffect(1));
    }
}
