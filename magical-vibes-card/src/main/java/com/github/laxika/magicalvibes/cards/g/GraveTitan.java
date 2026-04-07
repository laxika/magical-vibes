package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M11", collectorNumber = "97")
public class GraveTitan extends Card {

    public GraveTitan() {
        // Whenever Grave Titan enters the battlefield or attacks,
        // create two 2/2 black Zombie creature tokens.
        CreateTokenEffect tokenEffect = CreateTokenEffect.blackZombie(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, tokenEffect);
        addEffect(EffectSlot.ON_ATTACK, tokenEffect);
    }
}
