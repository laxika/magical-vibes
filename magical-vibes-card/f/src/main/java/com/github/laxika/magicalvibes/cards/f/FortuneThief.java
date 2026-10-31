package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;
import com.github.laxika.magicalvibes.model.effect.LifeFloorCondition;

@CardRegistration(set = "TSP", collectorNumber = "156")
@CardRegistration(set = "A25", collectorNumber = "130")
public class FortuneThief extends Card {

    public FortuneThief() {
        addMorph("{R}{R}");
        addStaticEffect(new DamageLifeFloorEffect(1, LifeFloorCondition.ALWAYS));
    }
}
