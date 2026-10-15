package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CountAsCreaturesEffect;

@CardRegistration(set = "MB1", collectorNumber = "5")
public class FiveKidsInATrenchcoat extends Card {

    public FiveKidsInATrenchcoat() {
        // Five Kids in a Trenchcoat counts as five creatures for spells and effects that count
        // the number of creatures you control.
        addEffect(EffectSlot.STATIC, new CountAsCreaturesEffect(5));
    }
}
