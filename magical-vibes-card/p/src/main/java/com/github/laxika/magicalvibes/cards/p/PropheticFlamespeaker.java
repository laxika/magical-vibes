package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "106")
public class PropheticFlamespeaker extends Card {

    public PropheticFlamespeaker() {
        // Whenever this creature deals combat damage to a player, exile the top card of your library.
        // Until end of turn, you may play that card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
