package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDiscardedCardThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH2", collectorNumber = "57")
public class Recalibrate extends Card {

    public Recalibrate() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target())
                .addEffect(EffectSlot.SPELL,
                        new ConditionalEffect(new ControllerDiscardedCardThisTurn(), new DrawCardEffect()));
    }
}
