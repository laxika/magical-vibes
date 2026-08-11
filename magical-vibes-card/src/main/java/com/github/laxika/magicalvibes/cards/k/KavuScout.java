package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "INV", collectorNumber = "151")
public class KavuScout extends Card {

    public KavuScout() {
        // Domain — This creature gets +1/+0 for each basic land type among lands you control.
        addEffect(EffectSlot.STATIC,
                new BoostSelfEffect(new BasicLandTypesAmongControlledLands(), new Fixed(0)));
    }
}
