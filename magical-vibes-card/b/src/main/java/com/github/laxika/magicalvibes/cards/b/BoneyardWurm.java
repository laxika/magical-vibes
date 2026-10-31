package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ISD", collectorNumber = "171")
@CardRegistration(set = "DDJ", collectorNumber = "51")
@CardRegistration(set = "UMA", collectorNumber = "159")
public class BoneyardWurm extends Card {

    public BoneyardWurm() {
        CardsInGraveyard creaturesInGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(creaturesInGraveyard, creaturesInGraveyard));
    }
}
