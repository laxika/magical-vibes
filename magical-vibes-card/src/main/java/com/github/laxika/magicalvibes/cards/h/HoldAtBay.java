package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "BNG", collectorNumber = "18")
public class HoldAtBay extends Card {

    public HoldAtBay() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.nextToTarget(7));
    }
}
