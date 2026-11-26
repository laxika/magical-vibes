package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ELD", collectorNumber = "92")
public class LashOfThorns extends Card {

    public LashOfThorns() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.target()));
    }
}
