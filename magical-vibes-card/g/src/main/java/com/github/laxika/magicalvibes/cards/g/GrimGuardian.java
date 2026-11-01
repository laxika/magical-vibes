package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JOU", collectorNumber = "73")
public class GrimGuardian extends Card {

    public GrimGuardian() {
        // Constellation — Whenever this creature or another enchantment you control enters,
        // each opponent loses 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT));
    }
}
