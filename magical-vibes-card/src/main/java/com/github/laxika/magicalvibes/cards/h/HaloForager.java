package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PayXManaCastTargetInstantOrSorceryFromGraveyardEffect;

@CardRegistration(set = "MOM", collectorNumber = "227")
public class HaloForager extends Card {

    public HaloForager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PayXManaCastTargetInstantOrSorceryFromGraveyardEffect());
    }
}
