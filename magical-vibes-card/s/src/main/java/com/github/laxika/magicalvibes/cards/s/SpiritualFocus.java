package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCausedDiscardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MMQ", collectorNumber = "49")
public class SpiritualFocus extends Card {

    public SpiritualFocus() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new OpponentCausedDiscardTriggerEffect(
                SequenceEffect.of(
                        new GainLifeEffect(2),
                        new MayEffect(new DrawCardEffect(), "Draw a card?"))));
    }
}
