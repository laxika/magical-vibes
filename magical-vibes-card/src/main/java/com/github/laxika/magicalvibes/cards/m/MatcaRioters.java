package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "CON", collectorNumber = "84")
public class MatcaRioters extends Card {

    public MatcaRioters() {
        // Domain — power and toughness are each equal to the number of basic land types among lands you control.
        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(domain, domain));
    }
}
