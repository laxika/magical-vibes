package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "8ED", collectorNumber = "297")
public class DingusEgg extends Card {

    public DingusEgg() {
        // Whenever a land is put into a graveyard from the battlefield, deal 2 damage to that land's controller.
        addEffect(EffectSlot.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
