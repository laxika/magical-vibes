package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "NEO", collectorNumber = "224")
@CardRegistration(set = "FCA", collectorNumber = "54")
@CardRegistration(set = "SLD", collectorNumber = "2459")
public class IsshinTwoHeavensAsOne extends Card {

    public IsshinTwoHeavensAsOne() {
        addStaticEffect(new AdditionalTriggeredAbilityEffect(
                new PermanentTruePredicate(), null, true, true, false, false));
    }
}
