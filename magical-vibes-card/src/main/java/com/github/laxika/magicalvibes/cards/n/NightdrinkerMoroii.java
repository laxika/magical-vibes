package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "MKM", collectorNumber = "96")
public class NightdrinkerMoroii extends Card {

    public NightdrinkerMoroii() {
        addMorph("{B}{B}");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(3));
    }
}
