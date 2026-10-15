package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect;

@CardRegistration(set = "PLS", collectorNumber = "28")
public class PlanarOverlay extends Card {

    public PlanarOverlay() {
        addEffect(EffectSlot.SPELL, new EachPlayerChoosesLandOfEachBasicTypeThenReturnToHandEffect());
    }
}
