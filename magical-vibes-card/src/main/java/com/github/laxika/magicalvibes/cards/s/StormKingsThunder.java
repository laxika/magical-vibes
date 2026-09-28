package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnForXValueEffect;

@CardRegistration(set = "HBG", collectorNumber = "189")
public class StormKingsThunder extends Card {

    public StormKingsThunder() {
        addEffect(EffectSlot.SPELL, new CopyNextInstantOrSorceryCastThisTurnForXValueEffect());
    }
}
