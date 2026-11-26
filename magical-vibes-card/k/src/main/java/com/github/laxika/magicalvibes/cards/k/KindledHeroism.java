package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "138")
public class KindledHeroism extends Card {

    public KindledHeroism() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(1, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()))
                .addSpellEffect(new ScryEffect(1));
    }
}
