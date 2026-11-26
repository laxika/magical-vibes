package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "142")
public class SamutsSprint extends Card {

    public SamutsSprint() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()))
                .addSpellEffect(new ScryEffect(1));
    }
}
