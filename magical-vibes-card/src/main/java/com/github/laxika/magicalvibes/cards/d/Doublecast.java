package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;

@CardRegistration(set = "M19", collectorNumber = "137")
public class Doublecast extends Card {

    public Doublecast() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnEffect());
    }
}
