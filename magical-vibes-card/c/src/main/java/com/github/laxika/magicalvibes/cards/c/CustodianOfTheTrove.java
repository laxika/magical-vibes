package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "DTK", collectorNumber = "236")
public class CustodianOfTheTrove extends Card {

    public CustodianOfTheTrove() {
        addStaticEffect(new EntersTappedEffect());
    }
}
