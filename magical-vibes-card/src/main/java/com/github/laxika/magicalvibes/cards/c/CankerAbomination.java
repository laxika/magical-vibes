package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "EVE", collectorNumber = "115")
public class CankerAbomination extends Card {

    public CankerAbomination() {
        // As Canker Abomination enters, choose an opponent. It enters with a -1/-1
        // counter on it for each creature that player controls. In the two-player
        // engine the chosen opponent is the single opponent, so OPPONENTS scope
        // counts exactly the creatures that player controls.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.MINUS_ONE_MINUS_ONE,
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS)));
    }
}
