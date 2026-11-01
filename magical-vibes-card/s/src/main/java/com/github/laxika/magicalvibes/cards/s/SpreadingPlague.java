package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "125")
public class SpreadingPlague extends Card {

    public SpreadingPlague() {
        addTrigger(Triggers.enters(null, ObjectRelation.ANY),
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.CREATURE),
                        new DestroyOtherCreaturesSharingColorWithEnteringCreatureEffect()));
    }
}
