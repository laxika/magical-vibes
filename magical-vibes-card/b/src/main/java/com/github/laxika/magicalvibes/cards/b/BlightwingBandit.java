package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileExiledEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOC", collectorNumber = "13")
@CardRegistration(set = "WOC", collectorNumber = "49")
public class BlightwingBandit extends Card {

    public BlightwingBandit() {
        // Whenever you cast your first spell during each opponent's turn, look at the top card of
        // that player's library, then exile it face down. You may play that card for as long as
        // it remains exiled, and mana of any type can be spent to cast it.
        addTrigger(Triggers.youCastSpell().duringOpponentsTurn().nthThisTurn(1),
                new ExileTopCardMayPlayWhileExiledEffect(true, true));
    }
}
