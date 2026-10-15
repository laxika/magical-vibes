package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "DMU", collectorNumber = "174")
public class NishobaBrawler extends Card {

    public NishobaBrawler() {
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new BasicLandTypesAmongControlledLands(), new Fixed(3)));
    }
}
