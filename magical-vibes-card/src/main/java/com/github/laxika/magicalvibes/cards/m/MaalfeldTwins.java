package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "AVR", collectorNumber = "112")
public class MaalfeldTwins extends Card {

    public MaalfeldTwins() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.blackZombie(2));
    }
}
