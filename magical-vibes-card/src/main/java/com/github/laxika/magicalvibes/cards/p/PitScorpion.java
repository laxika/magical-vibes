package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;

@CardRegistration(set = "5ED", collectorNumber = "187")
@CardRegistration(set = "4ED", collectorNumber = "153")
public class PitScorpion extends Card {

    public PitScorpion() {
        // Whenever this creature deals damage to a player, that player gets a poison counter.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER));
    }
}
