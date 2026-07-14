package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "EVE", collectorNumber = "109")
public class NoggleRansacker extends Card {

    public NoggleRansacker() {
        // When this creature enters, each player draws two cards, then discards a card at random.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EachPlayerDrawsCardEffect(2));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardEffect(1, DiscardRecipient.EACH_PLAYER, true));
    }
}
