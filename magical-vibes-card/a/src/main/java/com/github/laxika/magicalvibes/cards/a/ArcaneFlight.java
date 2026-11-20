package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "43")
public class ArcaneFlight extends Card {

    public ArcaneFlight() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(1, 1, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()));
    }
}
