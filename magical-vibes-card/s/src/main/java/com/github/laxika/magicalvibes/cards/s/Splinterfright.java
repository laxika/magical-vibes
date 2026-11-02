package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "205")
@CardRegistration(set = "INR", collectorNumber = "217")
@CardRegistration(set = "INR", collectorNumber = "420")
@CardRegistration(set = "2X2", collectorNumber = "159")
@CardRegistration(set = "C20", collectorNumber = "191")
public class Splinterfright extends Card {

    public Splinterfright() {
        CardsInGraveyard creaturesInGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(creaturesInGraveyard, creaturesInGraveyard));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
