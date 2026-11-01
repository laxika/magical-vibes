package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "53")
public class ThassasDevourer extends Card {

    public ThassasDevourer() {
        // Constellation — Whenever this creature or another enchantment you control enters,
        // target player mills two cards.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
