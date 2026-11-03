package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "133")
@CardRegistration(set = "KLR", collectorNumber = "148")
public class SpiresideInfiltrator extends Card {

    public SpiresideInfiltrator() {
        // Whenever this creature becomes tapped, it deals 1 damage to each opponent.
        addTrigger(Triggers.becomesTapped(), new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
