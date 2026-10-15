package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "BLB", collectorNumber = "123")
public class AlaniasPathmaker extends Card {

    public AlaniasPathmaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
