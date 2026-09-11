package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "6ED", collectorNumber = "273")
@CardRegistration(set = "5ED", collectorNumber = "348")
@CardRegistration(set = "4ED", collectorNumber = "294")
@CardRegistration(set = "3ED", collectorNumber = "233")
@CardRegistration(set = "SUM", collectorNumber = "233")
@CardRegistration(set = "ME1", collectorNumber = "151")
@CardRegistration(set = "VMA", collectorNumber = "263")
public class AnkhOfMishra extends Card {

    public AnkhOfMishra() {
        // Whenever a land enters, this artifact deals 2 damage to that land's controller.
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
