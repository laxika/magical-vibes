package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "CON", collectorNumber = "4")
public class AvenTrailblazer extends Card {

    public AvenTrailblazer() {
        // Domain — toughness equals the number of basic land types among lands you control.
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(new Fixed(2), new BasicLandTypesAmongControlledLands()));
    }
}
