package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "SOK", collectorNumber = "127")
public class DosansOldestChant extends Card {

    public DosansOldestChant() {
        addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new GainLifeEffect(6),
                new DrawCardEffect(1)));
    }
}
