package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "115")
@CardRegistration(set = "GK2", collectorNumber = "64")
public class JaggedPoppet extends Card {

    public JaggedPoppet() {
        addTrigger(Triggers.dealtDamage(),
                new DiscardEffect(new EventValue(), DiscardRecipient.CONTROLLER));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerHandEmpty()),
                new DiscardEffect(new EventValue(), DiscardRecipient.TARGET_PLAYER));
    }
}
