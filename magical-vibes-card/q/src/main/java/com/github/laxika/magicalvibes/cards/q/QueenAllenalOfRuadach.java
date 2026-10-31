package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AddSoldierTokenToCreatureTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DMU", collectorNumber = "210")
public class QueenAllenalOfRuadach extends Card {

    public QueenAllenalOfRuadach() {
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(creaturesYouControl, creaturesYouControl));
        addStaticEffect(new AddSoldierTokenToCreatureTokenCreationEffect());
    }
}
