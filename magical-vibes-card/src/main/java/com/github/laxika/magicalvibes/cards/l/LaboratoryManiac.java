package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WinGameOnEmptyLibraryDrawEffect;

@CardRegistration(set = "ISD", collectorNumber = "61")
public class LaboratoryManiac extends Card {

    public LaboratoryManiac() {
        addEffect(EffectSlot.STATIC, new WinGameOnEmptyLibraryDrawEffect());
    }
}
