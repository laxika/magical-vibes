package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "DOM", collectorNumber = "58")
public class NabanDeanOfIteration extends Card {

    public NabanDeanOfIteration() {
        addStaticEffect(new ETBDoubleTriggerEffect(new CardSubtypePredicate(CardSubtype.WIZARD)));
    }
}
