package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "P02", collectorNumber = "108")
public class MagmaGiant extends Card {

    public MagmaGiant() {
        // When this creature enters, it deals 2 damage to each creature and each player.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(2, true));
    }
}
