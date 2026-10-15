package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "ISD", collectorNumber = "157")
public class RageThrower extends Card {

    public RageThrower() {
        // Whenever another creature dies, Rage Thrower deals 2 damage to target player or planeswalker.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
    }
}
