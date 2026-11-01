package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "31")
public class PerimeterEnforcer extends Card {

    public PerimeterEnforcer() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DETECTIVE),
                        new BoostSelfEffect(1, 1)));

        addTrigger(Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DETECTIVE),
                        new BoostSelfEffect(1, 1)));
    }
}
