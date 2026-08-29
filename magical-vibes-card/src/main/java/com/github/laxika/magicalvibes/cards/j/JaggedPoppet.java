package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "DIS", collectorNumber = "115")
public class JaggedPoppet extends Card {

    public JaggedPoppet() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new DiscardEffect(new EventValue(), DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ConditionalEffect(new ControllerHandEmpty(),
                        new DiscardEffect(new EventValue(), DiscardRecipient.TARGET_PLAYER)));
    }
}
