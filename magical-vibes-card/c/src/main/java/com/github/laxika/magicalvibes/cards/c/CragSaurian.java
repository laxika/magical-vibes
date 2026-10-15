package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfDamagedPermanentEffect;

@CardRegistration(set = "MMQ", collectorNumber = "185")
public class CragSaurian extends Card {

    public CragSaurian() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DamageSourceControllerGainsControlOfDamagedPermanentEffect());
    }
}
