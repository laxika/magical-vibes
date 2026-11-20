package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Equipped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MSC", collectorNumber = "647")
public class ArmedAssailant extends Card {

    public ArmedAssailant() {
        addStaticEffect(new ConditionalEffect(new Equipped(),
                new StaticBoostEffect(2, 0, Subjects.source())));
        addStaticEffect(new ConditionalEffect(new Equipped(),
                new GrantKeywordEffect(Keyword.MENACE, Subjects.source())));
    }
}
