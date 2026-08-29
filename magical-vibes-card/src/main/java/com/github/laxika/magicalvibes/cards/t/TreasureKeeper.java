package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandCardWithManaValueAndCastEffect;

@CardRegistration(set = "AER", collectorNumber = "177")
public class TreasureKeeper extends Card {

    public TreasureKeeper() {
        addEffect(EffectSlot.ON_DEATH, new RevealUntilNonlandCardWithManaValueAndCastEffect(3));
    }
}
