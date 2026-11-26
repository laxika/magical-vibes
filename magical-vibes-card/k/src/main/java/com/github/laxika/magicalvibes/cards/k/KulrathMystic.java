package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "56")
public class KulrathMystic extends Card {

    public KulrathMystic() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(4)),
                BoostEffect.self(2, 0),
                new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.source()));
    }
}
