package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachCreatureDealsPowerDamageToItselfEffect;

@CardRegistration(set = "MMQ", collectorNumber = "56")
public class WaveOfReckoning extends Card {

    public WaveOfReckoning() {
        addEffect(EffectSlot.SPELL, new EachCreatureDealsPowerDamageToItselfEffect());
    }
}
