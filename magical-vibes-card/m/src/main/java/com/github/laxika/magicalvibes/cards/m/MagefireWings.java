package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "88")
public class MagefireWings extends Card {

    public MagefireWings() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(2, 0, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()));
    }
}
