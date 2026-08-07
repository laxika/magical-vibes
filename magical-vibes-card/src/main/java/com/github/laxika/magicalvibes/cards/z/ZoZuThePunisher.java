package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

/**
 * Zo-Zu the Punisher — "Whenever a land enters, Zo-Zu deals 2 damage to that land's controller."
 *
 * <p>The engine splits "a land enters" into an ally slot and an opponent slot; both are filled so
 * every land entering the battlefield is covered, and each deals its 2 damage to the land's own
 * controller.
 */
@CardRegistration(set = "CHK", collectorNumber = "200")
public class ZoZuThePunisher extends Card {

    public ZoZuThePunisher() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
    }
}
