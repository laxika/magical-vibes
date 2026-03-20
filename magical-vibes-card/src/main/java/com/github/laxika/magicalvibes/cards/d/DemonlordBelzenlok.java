package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileUntilNonlandToHandRepeatIfHighMVEffect;

@CardRegistration(set = "DOM", collectorNumber = "86")
public class DemonlordBelzenlok extends Card {

    public DemonlordBelzenlok() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileUntilNonlandToHandRepeatIfHighMVEffect(4, 1));
    }
}
