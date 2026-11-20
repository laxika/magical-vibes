package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "116")
public class GiantSpectacle extends Card {

    public GiantSpectacle() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(2, 1, Subjects.attachedCreature()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, Subjects.attachedCreature()));
    }
}
