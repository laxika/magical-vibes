package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToFlyingAndPlayersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "270")
public class Hurricane extends Card {

    public Hurricane() {
        addEffect(EffectSlot.SPELL, new DealDamageToFlyingAndPlayersEffect());
    }
}
