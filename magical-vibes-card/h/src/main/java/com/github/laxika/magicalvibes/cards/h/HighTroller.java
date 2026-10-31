package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseTargetsAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceTargetedCostEffect;

@CardRegistration(set = "MB1", collectorNumber = "54")
public class HighTroller extends Card {

    public HighTroller() {
        addStaticEffect(new ReduceTargetedCostEffect(2));
        addStaticEffect(new ChooseTargetsAtRandomEffect());
    }
}
