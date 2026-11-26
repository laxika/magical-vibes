package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "147")
public class DesperateStand extends Card {

    public DesperateStand() {
        setAdditionalManaCostPerExtraTarget("{R}{W}");

        target(TargetFilters.creature(), 0, Integer.MAX_VALUE)
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.target()));
    }
}
