package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect;

@CardRegistration(set = "MH2", collectorNumber = "30")
public class SerrasEmissary extends Card {

    public SerrasEmissary() {
        addStaticEffect(new ChooseCardTypeOnEnterEffect());
        addStaticEffect(new GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect());
    }
}
