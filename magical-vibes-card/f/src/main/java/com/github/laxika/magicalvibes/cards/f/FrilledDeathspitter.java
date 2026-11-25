package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "104")
public class FrilledDeathspitter extends Card {

    public FrilledDeathspitter() {
        // Enrage — Whenever this creature is dealt damage, it deals 2 damage to target opponent or planeswalker.
        addTrigger(Triggers.dealtDamage(),
                DealDamageEffect.toTargetOpponentOrPlaneswalker(2));
    }
}
