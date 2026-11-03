package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "77")
public class TanufelRimespeaker extends Card {

    public TanufelRimespeaker() {
        addTrigger(Triggers.youCastSpell().withFilter(new CardMinManaValuePredicate(4)), new DrawCardEffect());
    }
}
