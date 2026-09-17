package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WinGameOnEmptyLibraryDrawEffect;

@CardRegistration(set = "ISD", collectorNumber = "61")
@CardRegistration(set = "TSR", collectorNumber = "309")
@CardRegistration(set = "INR", collectorNumber = "71")
@CardRegistration(set = "INR", collectorNumber = "304")
@CardRegistration(set = "INR", collectorNumber = "359")
@CardRegistration(set = "UMA", collectorNumber = "61")
@CardRegistration(set = "FCA", collectorNumber = "30")
@CardRegistration(set = "SLZ", collectorNumber = "21")
@CardRegistration(set = "SLZ", collectorNumber = "142")
@CardRegistration(set = "SLZ", collectorNumber = "263")
public class LaboratoryManiac extends Card {

    public LaboratoryManiac() {
        addEffect(EffectSlot.STATIC, new WinGameOnEmptyLibraryDrawEffect());
    }
}
