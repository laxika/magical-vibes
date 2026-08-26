package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MID", collectorNumber = "54")
public class FlipTheSwitch extends Card {

    public FlipTheSwitch() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(4));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
