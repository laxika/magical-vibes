package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;

@CardRegistration(set = "APC", collectorNumber = "134")
@CardRegistration(set = "TSB", collectorNumber = "108")
public class Dodecapod extends Card {

    public Dodecapod() {
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new EnterBattlefieldOnDiscardEffect(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
