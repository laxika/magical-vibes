package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

@CardRegistration(set = "SHM", collectorNumber = "129")
public class Spawnwrithe extends Card {

    public Spawnwrithe() {
        // Trample (keyword auto-loaded from oracle data).
        // Whenever this creature deals combat damage to a player, create a token
        // that's a copy of this creature (the copy has the same trigger).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CreateTokenCopyOfSourceEffect());
    }
}
