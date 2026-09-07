package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

@CardRegistration(set = "JUD", collectorNumber = "29")
public class TestOfEndurance extends Card {

    public TestOfEndurance() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControllerLifeAtLeast(50), new WinGameEffect()));
    }
}
