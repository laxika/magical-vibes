package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "5ED", collectorNumber = "315")
public class MarshViper extends Card {

    public MarshViper() {
        // Whenever this creature deals damage to a player, that player gets two poison counters.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER));
    }
}
