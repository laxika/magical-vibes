package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "143")
public class RavenousGiant extends Card {

    public RavenousGiant() {
        // At the beginning of your upkeep, this creature deals 1 damage to you.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
    }
}
