package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

@CardRegistration(set = "ZEN", collectorNumber = "168")
public class LotusCobra extends Card {

    public LotusCobra() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new AwardAnyColorManaEffect());
    }
}
