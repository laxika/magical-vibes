package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnPermanentsOrPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.HalveCountersPutByOpponentsEffect;

@CardRegistration(set = "KHM", collectorNumber = "199")
@CardRegistration(set = "KHM", collectorNumber = "320")
@CardRegistration(set = "KHM", collectorNumber = "333")
@CardRegistration(set = "KHM", collectorNumber = "406")
@CardRegistration(set = "KHM", collectorNumber = "407")
public class VorinclexMonstrousRaider extends Card {

    public VorinclexMonstrousRaider() {
        addEffect(EffectSlot.STATIC, new DoubleCountersOnPermanentsOrPlayersEffect());
        addEffect(EffectSlot.STATIC, new HalveCountersPutByOpponentsEffect());
    }
}
