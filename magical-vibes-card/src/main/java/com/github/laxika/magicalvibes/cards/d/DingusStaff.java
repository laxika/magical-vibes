package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "WTH", collectorNumber = "149")
public class DingusStaff extends Card {

    public DingusStaff() {
        // Whenever a creature dies, this artifact deals 2 damage to that creature's controller.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
