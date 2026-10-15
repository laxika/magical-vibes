package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "DIS", collectorNumber = "121")
public class PainMagnification extends Card {

    public PainMagnification() {
        addEffect(EffectSlot.ON_OPPONENT_DEALT_DAMAGE,
                new ConditionalEffect(new EventValueAtLeast(3),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
