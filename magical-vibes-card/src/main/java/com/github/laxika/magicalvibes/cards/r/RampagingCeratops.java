package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;

@CardRegistration(set = "LCI", collectorNumber = "162")
public class RampagingCeratops extends Card {

    public RampagingCeratops() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByFewerThanNCreaturesEffect(3));
    }
}
