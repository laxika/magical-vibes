package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect;

@CardRegistration(set = "GTC", collectorNumber = "63")
public class DevourFlesh extends Card {

    public DevourFlesh() {
        addEffect(EffectSlot.SPELL,
                new SacrificeCreatureAndControllerGainsLifeEqualToToughnessEffect(false, true));
    }
}
