package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongCardsInGraveyard;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M21", collectorNumber = "92")
@CardRegistration(set = "CMM", collectorNumber = "143")
@CardRegistration(set = "DSC", collectorNumber = "134")
public class CarrionGrub extends Card {

    public CarrionGrub() {
        addStaticEffect(new BoostSelfEffect(
                new GreatestPowerAmongCardsInGraveyard(
                        new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER),
                new Fixed(0)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(4, MillRecipient.CONTROLLER));
    }
}
