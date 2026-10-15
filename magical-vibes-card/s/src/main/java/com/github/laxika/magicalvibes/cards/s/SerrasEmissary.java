package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect;

@CardRegistration(set = "MH2", collectorNumber = "30")
public class SerrasEmissary extends Card {

    public SerrasEmissary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardTypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, new GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect());
    }
}
