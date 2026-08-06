package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HighestLifeTotalAmongPlayers;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;

@CardRegistration(set = "LRW", collectorNumber = "2")
public class ArbiterOfKnollridge extends Card {

    public ArbiterOfKnollridge() {
        // When this creature enters, each player's life total becomes the highest life total among all players.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SetLifeTotalEffect(
                new HighestLifeTotalAmongPlayers(), SetLifeTotalRecipient.EACH_PLAYER));
    }
}
