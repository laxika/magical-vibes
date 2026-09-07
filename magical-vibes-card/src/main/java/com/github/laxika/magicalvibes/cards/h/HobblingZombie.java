package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MID", collectorNumber = "106")
public class HobblingZombie extends Card {

    public HobblingZombie() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
