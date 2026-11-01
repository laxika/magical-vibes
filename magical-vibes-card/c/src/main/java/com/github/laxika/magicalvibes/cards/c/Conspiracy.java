package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;

@CardRegistration(set = "MMQ", collectorNumber = "127")
@CardRegistration(set = "TSB", collectorNumber = "39")
@CardRegistration(set = "ACR", collectorNumber = "88")
public class Conspiracy extends Card {

    public Conspiracy() {
        addStaticEffect(new ChooseSubtypeOnEnterEffect());
        addStaticEffect(GrantChosenSubtypeToOwnCreaturesEffect.replacingInAllZones());
    }
}
