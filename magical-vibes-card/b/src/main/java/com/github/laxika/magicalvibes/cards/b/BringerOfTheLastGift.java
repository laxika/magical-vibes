package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreaturesThenReturnCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "94")
@CardRegistration(set = "LCI", collectorNumber = "337")
public class BringerOfTheLastGift extends Card {

    public BringerOfTheLastGift() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(new WasCast(true),
                new SacrificeOtherCreaturesThenReturnCreatureCardsEffect()));
    }
}
