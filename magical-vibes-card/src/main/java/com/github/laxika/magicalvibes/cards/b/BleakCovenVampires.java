package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeAndControllerGainsLifeEffect;

@CardRegistration(set = "SOM", collectorNumber = "55")
public class BleakCovenVampires extends Card {

    public BleakCovenVampires() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MetalcraftConditionalEffect(
                        new TargetPlayerLosesLifeAndControllerGainsLifeEffect(4, 4)));
    }
}
