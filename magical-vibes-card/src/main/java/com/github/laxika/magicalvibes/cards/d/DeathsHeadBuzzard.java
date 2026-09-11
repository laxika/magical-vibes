package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "SCG", collectorNumber = "63")
public class DeathsHeadBuzzard extends Card {

    public DeathsHeadBuzzard() {
        addEffect(EffectSlot.ON_DEATH, new BoostAllCreaturesEffect(-1, -1));
    }
}
