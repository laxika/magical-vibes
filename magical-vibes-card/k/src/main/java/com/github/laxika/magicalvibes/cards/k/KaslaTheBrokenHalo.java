package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "4")
@CardRegistration(set = "MOC", collectorNumber = "92")
@CardRegistration(set = "MOC", collectorNumber = "137")
public class KaslaTheBrokenHalo extends Card {

    public KaslaTheBrokenHalo() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardKeywordPredicate(Keyword.CONVOKE)),
                new ScryEffect(2),
                new DrawCardEffect(1));
    }
}
