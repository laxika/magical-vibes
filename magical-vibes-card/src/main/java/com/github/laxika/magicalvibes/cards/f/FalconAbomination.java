package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "MID", collectorNumber = "52")
public class FalconAbomination extends Card {

    public FalconAbomination() {
        // When this creature enters, create a 2/2 black Zombie creature token with decayed.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
