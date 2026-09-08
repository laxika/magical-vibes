package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnAurasFromGraveyardAttachedToCreaturesEffect;

@CardRegistration(set = "THB", collectorNumber = "156")
public class StormHerald extends Card {

    public StormHerald() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnAurasFromGraveyardAttachedToCreaturesEffect());
    }
}
