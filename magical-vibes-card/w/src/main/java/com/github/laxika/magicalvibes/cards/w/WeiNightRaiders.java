package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "89")
@CardRegistration(set = "ME3", collectorNumber = "81")
public class WeiNightRaiders extends Card {

    public WeiNightRaiders() {
        // Whenever this creature deals damage to an opponent, that player discards a card.
        addTrigger(Triggers.dealsDamageToOpponent(), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
