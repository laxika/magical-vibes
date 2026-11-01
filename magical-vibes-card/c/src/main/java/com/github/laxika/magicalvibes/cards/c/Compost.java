package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "7ED", collectorNumber = "235")
@CardRegistration(set = "UDS", collectorNumber = "102")
@CardRegistration(set = "SLD", collectorNumber = "1047")
public class Compost extends Card {

    public Compost() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.BLACK_CARD, ObjectRelation.OPPONENT_OWNS),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
