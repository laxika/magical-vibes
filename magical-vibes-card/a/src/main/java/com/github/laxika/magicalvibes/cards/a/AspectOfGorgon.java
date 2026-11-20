package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "60")
public class AspectOfGorgon extends Card {

    public AspectOfGorgon() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 3, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.DEATHTOUCH, Subjects.attachedCreature()));
    }
}
