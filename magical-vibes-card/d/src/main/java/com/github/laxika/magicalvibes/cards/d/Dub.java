package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "15")
@CardRegistration(set = "M21", collectorNumber = "16")
public class Dub extends Card {

    public Dub() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.attachedCreature()))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.KNIGHT, Subjects.attachedCreature()));
    }
}
