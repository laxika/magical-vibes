package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Zo-Zu the Punisher — "Whenever a land enters, Zo-Zu deals 2 damage to that land's controller."
 *
 * <p>The engine splits "a land enters" into an ally slot and an opponent slot; both are filled so
 * every land entering the battlefield is covered, and each deals its 2 damage to the land's own
 * controller.
 */
@CardRegistration(set = "CHK", collectorNumber = "200")
@CardRegistration(set = "DDS", collectorNumber = "41")
@CardRegistration(set = "SLC", collectorNumber = "26")
@CardRegistration(set = "SLC", collectorNumber = "53")
public class ZoZuThePunisher extends Card {

    public ZoZuThePunisher() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
