package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "43")
public class CalamitousTide extends Card {

    public CalamitousTide() {
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(2))
                .addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
