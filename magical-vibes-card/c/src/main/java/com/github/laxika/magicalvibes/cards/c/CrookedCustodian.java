package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "SNC", collectorNumber = "71")
public class CrookedCustodian extends Card {

    public CrookedCustodian() {
        addStaticEffect(new EntersTappedEffect());
    }
}
