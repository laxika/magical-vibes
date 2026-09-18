package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "DMU", collectorNumber = "184")
public class TerritorialMaro extends Card {

    public TerritorialMaro() {
        // Domain — Territorial Maro's power and toughness are each equal to twice the number
        // of basic land types among lands you control.
        Scaled domain = new Scaled(new BasicLandTypesAmongControlledLands(), 2);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(domain, domain));
    }
}
