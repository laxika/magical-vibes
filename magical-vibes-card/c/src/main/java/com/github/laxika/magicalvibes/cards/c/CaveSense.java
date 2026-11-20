package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "179")
public class CaveSense extends Card {

    public CaveSense() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.MOUNTAINWALK, Subjects.attachedCreature()));
    }
}
