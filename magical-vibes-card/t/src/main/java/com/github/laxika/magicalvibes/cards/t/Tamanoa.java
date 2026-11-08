package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "132")
public class Tamanoa extends Card {

    public Tamanoa() {
        // Whenever a noncreature source you control deals damage, you gain that much life.
        addTrigger(Triggers.sourceYouControlDealsDamage()
                        .withFilter(new PermanentNotPredicate(new PermanentIsCreaturePredicate()))
                        .withSpellFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                new GainLifeEffect(new EventValue()));
    }
}
