package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "IKO", collectorNumber = "208")
public class Slitherwisp extends Card {

    public Slitherwisp() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardKeywordPredicate(Keyword.FLASH)),
                new DrawCardEffect(1),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
