package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RestartTurnEffect;

@CardRegistration(set = "MB1", collectorNumber = "20")
public class DoOver extends Card {

    public DoOver() {
        addEffect(EffectSlot.SPELL, new RestartTurnEffect());
    }
}
