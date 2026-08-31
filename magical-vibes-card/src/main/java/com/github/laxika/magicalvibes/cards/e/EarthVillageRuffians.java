package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;

@CardRegistration(set = "TLA", collectorNumber = "219")
public class EarthVillageRuffians extends Card {

    public EarthVillageRuffians() {
        addEffect(EffectSlot.ON_DEATH, new EarthbendTargetLandEffect(2));
    }
}
