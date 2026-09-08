package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysLifeEffect;

@CardRegistration(set = "BRO", collectorNumber = "121")
public class PhyrexianFleshgorger extends Card {

    public PhyrexianFleshgorger() {
        addPrototype("{1}{B}{B}", CardColor.BLACK, 3, 3);
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysLifeEffect(new SourcePower()));
    }
}
