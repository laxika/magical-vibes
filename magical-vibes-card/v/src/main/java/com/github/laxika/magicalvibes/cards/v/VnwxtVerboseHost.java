package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.MaxSpeed;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;

@CardRegistration(set = "DFT", collectorNumber = "73")
public class VnwxtVerboseHost extends Card {

    public VnwxtVerboseHost() {
        addStaticEffect(new NoMaximumHandSizeEffect());
        addStaticEffect(new ConditionalEffect(
                new MaxSpeed(), new DoubleDrawReplacementEffect()));
    }
}
