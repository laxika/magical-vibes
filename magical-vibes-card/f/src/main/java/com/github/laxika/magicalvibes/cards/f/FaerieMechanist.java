package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CON", collectorNumber = "27")
@CardRegistration(set = "DDF", collectorNumber = "54")
@CardRegistration(set = "MM2", collectorNumber = "44")
@CardRegistration(set = "MMA", collectorNumber = "45")
@CardRegistration(set = "DDU", collectorNumber = "38")
@CardRegistration(set = "2XM", collectorNumber = "50")
public class FaerieMechanist extends Card {

    public FaerieMechanist() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), LookAtTopCardsEffect.mayRevealOneToHandRestOnBottom(3,
                new CardTypePredicate(CardType.ARTIFACT)));
    }
}
