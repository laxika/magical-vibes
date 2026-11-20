package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DFT", collectorNumber = "32")
public class SwiftwingAssailant extends Card {

    public SwiftwingAssailant() {
        addStaticEffect(new ConditionalEffect(new MaxSpeed(),
                new StaticBoostEffect(0, 1, Subjects.source())));
        addStaticEffect(new ConditionalEffect(new MaxSpeed(),
                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.source())));
    }
}
