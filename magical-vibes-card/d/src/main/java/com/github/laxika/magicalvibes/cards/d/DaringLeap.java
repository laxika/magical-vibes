package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PLS", collectorNumber = "101")
public class DaringLeap extends Card {

    public DaringLeap() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(1, 1))
          .addSpellEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.target()))
          .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()));
    }
}
