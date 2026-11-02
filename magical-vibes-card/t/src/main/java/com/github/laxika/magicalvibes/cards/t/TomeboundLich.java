package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "219")
@CardRegistration(set = "PIO", collectorNumber = "249")
public class TomeboundLich extends Card {

    public TomeboundLich() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), loot());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), loot());
    }

    private SequenceEffect loot() {
        return SequenceEffect.of(
                new DrawCardEffect(),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
