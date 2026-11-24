package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

@CardRegistration(set = "WAR", collectorNumber = "167")
@CardRegistration(set = "CMM", collectorNumber = "306")
public class MowuLoyalCompanion extends Card {

    public MowuLoyalCompanion() {
        addStaticEffect(AddOnePlusOneCountersEffect.plusOne(CounterType.PLUS_ONE_PLUS_ONE, new PermanentIsSourceCardPredicate()));
    }
}
