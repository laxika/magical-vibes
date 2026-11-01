package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "273")
@CardRegistration(set = "2ED", collectorNumber = "231")
@CardRegistration(set = "5ED", collectorNumber = "348")
@CardRegistration(set = "4ED", collectorNumber = "294")
@CardRegistration(set = "3ED", collectorNumber = "233")
@CardRegistration(set = "SUM", collectorNumber = "233")
@CardRegistration(set = "ME1", collectorNumber = "151")
@CardRegistration(set = "VMA", collectorNumber = "263")
@CardRegistration(set = "LEA", collectorNumber = "230")
@CardRegistration(set = "LEB", collectorNumber = "231")
public class AnkhOfMishra extends Card {

    public AnkhOfMishra() {
        // Whenever a land enters, this artifact deals 2 damage to that land's controller.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
