package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "85")
public class ThoughtrenderLamia extends Card {

    public ThoughtrenderLamia() {
        // Constellation — Whenever this creature or another enchantment you control enters,
        // each opponent discards a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
