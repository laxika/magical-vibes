package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CemeteryDesecratorEffect;

@CardRegistration(set = "VOW", collectorNumber = "100")
public class CemeteryDesecrator extends Card {

    public CemeteryDesecrator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CemeteryDesecratorEffect());
        addEffect(EffectSlot.ON_DEATH, new CemeteryDesecratorEffect());
    }
}
