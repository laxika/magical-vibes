package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "RIX", collectorNumber = "81")
public class PitilessPlunderer extends Card {

    public PitilessPlunderer() {
        // Whenever another creature you control dies, create a Treasure token.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, CreateTokenEffect.ofTreasureToken(1));
    }
}
