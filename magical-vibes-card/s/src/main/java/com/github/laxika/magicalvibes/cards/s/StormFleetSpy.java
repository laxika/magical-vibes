package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "84")
@CardRegistration(set = "FDN", collectorNumber = "515")
public class StormFleetSpy extends Card {

    public StormFleetSpy() {
        // Raid — When this creature enters, if you attacked this turn, draw a card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(new Raid(), new DrawCardEffect()));
    }
}
