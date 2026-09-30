package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithFewerCreaturesThanController;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "C19", collectorNumber = "36")
public class VoiceOfMany extends Card {

    public VoiceOfMany() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new OpponentsWithFewerCreaturesThanController()));
    }
}
