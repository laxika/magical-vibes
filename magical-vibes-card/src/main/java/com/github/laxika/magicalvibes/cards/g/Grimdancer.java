package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "90")
public class Grimdancer extends Card {

    public Grimdancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterTypeOnEnterEffect(2,
                        CounterType.MENACE, CounterType.DEATHTOUCH, CounterType.LIFELINK));
    }
}
