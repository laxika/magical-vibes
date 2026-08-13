package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostLifeGainsControlOfSourceCreatureEffect;

@CardRegistration(set = "USG", collectorNumber = "284")
public class WildDogs extends Card {

    public WildDogs() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PlayerWithMostLifeGainsControlOfSourceCreatureEffect());
        addCycling("{2}");
    }
}
