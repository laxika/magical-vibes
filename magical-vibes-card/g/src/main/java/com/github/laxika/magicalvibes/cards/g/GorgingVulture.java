package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "102")
public class GorgingVulture extends Card {

    public GorgingVulture() {
        // When this creature enters, mill four cards. You gain 1 life for each creature card
        // milled this way.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillControllerThenIfMilledEffect(
                4,
                new CardTypePredicate(CardType.CREATURE),
                new GainLifeEffect(new EventValue())));
    }
}
