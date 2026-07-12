package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "8ED", collectorNumber = "198")
public class LavaHounds extends Card {

    public LavaHounds() {
        // Haste is auto-loaded from Scryfall.
        // When this creature enters, it deals 4 damage to you.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToPlayersEffect(4, DamageRecipient.CONTROLLER));
    }
}
