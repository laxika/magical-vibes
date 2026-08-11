package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;

@CardRegistration(set = "ZEN", collectorNumber = "12")
public class FelidarSovereign extends Card {

    public FelidarSovereign() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControllerLifeAtLeast(40), new WinGameEffect()));
    }
}
