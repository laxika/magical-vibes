package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "LCI", collectorNumber = "121")
@CardRegistration(set = "LCI", collectorNumber = "369")
public class SoulsOfTheLost extends Card {

    public SoulsOfTheLost() {
        addCastingCost(AnyOfCost.of(new DiscardCardTypeCost(null, null),
                new SacrificePermanentCost(new PermanentTruePredicate(), "a permanent", false)));
        CardsInGraveyard permanentCards =
                new CardsInGraveyard(new CardIsPermanentPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                permanentCards, new Sum(permanentCards, new Fixed(1))));
    }
}
