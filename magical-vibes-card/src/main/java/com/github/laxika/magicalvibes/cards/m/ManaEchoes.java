package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManaEchoesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ONS", collectorNumber = "218")
public class ManaEchoes extends Card {

    public ManaEchoes() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD,
                new MayEffect(new ManaEchoesEffect(), "Add colorless mana?"));
    }
}
