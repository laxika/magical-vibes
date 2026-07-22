package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTakeDamageSacrificeSourceEffect;

@CardRegistration(set = "INR", collectorNumber = "178")
public class VexingDevil extends Card {

    public VexingDevil() {
        // When this creature enters, any opponent may have it deal 4 damage to them.
        // If a player does, sacrifice this creature.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AnyOpponentMayTakeDamageSacrificeSourceEffect(4));
    }
}
