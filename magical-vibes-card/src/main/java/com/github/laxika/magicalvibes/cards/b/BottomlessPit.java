package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "STH", collectorNumber = "51")
public class BottomlessPit extends Card {

    public BottomlessPit() {
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, true));
    }
}
