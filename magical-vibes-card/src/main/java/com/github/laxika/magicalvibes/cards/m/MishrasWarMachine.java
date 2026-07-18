package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageControllerUnlessDiscardThenTapSourceEffect;

@CardRegistration(set = "4ED", collectorNumber = "337")
public class MishrasWarMachine extends Card {

    public MishrasWarMachine() {
        // At the beginning of your upkeep, this creature deals 3 damage to you unless you discard a
        // card. If it deals damage to you this way, tap it.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new DamageControllerUnlessDiscardThenTapSourceEffect(3));
    }
}
