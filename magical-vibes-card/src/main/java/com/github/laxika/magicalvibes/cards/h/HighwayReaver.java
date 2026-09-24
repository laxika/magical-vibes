package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MaxSpeedFreeFirstUnearthEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantUnearthToTargetCreatureCardEffect;

@CardRegistration(set = "YDFT", collectorNumber = "22")
public class HighwayReaver extends Card {

    public HighwayReaver() {
        addEffect(EffectSlot.STATIC, new MaxSpeedFreeFirstUnearthEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PerpetuallyGrantUnearthToTargetCreatureCardEffect());
        addEffect(EffectSlot.ON_ATTACK,
                new PerpetuallyGrantUnearthToTargetCreatureCardEffect());
    }
}
