package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "43")
public class Drekavac extends Card {

    public Drekavac() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SacrificeUnlessDiscardCardTypeEffect.forPredicate(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        "noncreature card"));
    }
}
