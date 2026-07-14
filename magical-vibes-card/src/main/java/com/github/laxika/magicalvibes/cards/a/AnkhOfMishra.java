package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "6ED", collectorNumber = "273")
public class AnkhOfMishra extends Card {

    public AnkhOfMishra() {
        // Whenever a land enters, this artifact deals 2 damage to that land's controller.
        // Fires for any land: an opponent's land (damage to that opponent) and your own land (damage to you).
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new DealDamageToPlayersEffect(2, DamageRecipient.CONTROLLER));
    }
}
