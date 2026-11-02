package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "56")
@CardRegistration(set = "DDM", collectorNumber = "9")
@CardRegistration(set = "DDT", collectorNumber = "12")
public class MerfolkWayfinder extends Card {

    public MerfolkWayfinder() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LookAtTopCardsEffect(
                new Fixed(3), new Fixed(3), new CardSubtypePredicate(CardSubtype.ISLAND),
                LookDestination.BOTTOM_OF_LIBRARY, true));
    }
}
