package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "108")
public class MomentOfDefiance extends Card {

    public MomentOfDefiance() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 1))
                .addSpellEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.target()))
                .addSpellEffect(new DrawCardEffect());
    }
}
