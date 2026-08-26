package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DiscardCardOrSacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "LCI", collectorNumber = "121")
public class SoulsOfTheLost extends Card {

    public SoulsOfTheLost() {
        addEffect(EffectSlot.SPELL,
                new DiscardCardOrSacrificePermanentCost(new PermanentTruePredicate(), "a permanent"));
        CardsInGraveyard permanentCards =
                new CardsInGraveyard(new CardIsPermanentPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                permanentCards, new Sum(permanentCards, new Fixed(1))));
    }
}
