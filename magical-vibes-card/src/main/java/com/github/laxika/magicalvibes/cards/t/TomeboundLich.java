package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "M20", collectorNumber = "219")
public class TomeboundLich extends Card {

    public TomeboundLich() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, loot());
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, loot());
    }

    private SequenceEffect loot() {
        return SequenceEffect.of(
                new DrawCardEffect(),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
