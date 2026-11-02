package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "94")
@CardRegistration(set = "RVR", collectorNumber = "84")
public class MausoleumTurnkey extends Card {

    public MausoleumTurnkey() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnCardFromGraveyardToHandOfOpponentsChoiceEffect(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
