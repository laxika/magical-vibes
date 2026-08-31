package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ONS", collectorNumber = "14")
public class ConvalescentCare extends Card {

    public ConvalescentCare() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new ControllerLifeAtMost(5),
                        SequenceEffect.of(new GainLifeEffect(3), new DrawCardEffect())));
    }
}
