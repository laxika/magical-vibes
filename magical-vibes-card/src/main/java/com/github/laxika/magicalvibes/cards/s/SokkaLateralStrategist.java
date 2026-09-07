package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TLA", collectorNumber = "241")
public class SokkaLateralStrategist extends Card {

    public SokkaLateralStrategist() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(2),
                new DrawCardEffect()));
    }
}
