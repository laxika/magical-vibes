package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "34")
@CardRegistration(set = "MSC", collectorNumber = "333")
public class JarvisEarthsMightiestButler extends Card {

    public JarvisEarthsMightiestButler() {
        // Whenever you cast a Hero spell, draw a card.
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.HERO)),
                new DrawCardEffect());
    }
}
