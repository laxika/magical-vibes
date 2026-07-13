package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "8ED", collectorNumber = "148")
@CardRegistration(set = "7ED", collectorNumber = "148")
public class Nausea extends Card {

    public Nausea() {
        // All creatures get -1/-1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1));
    }
}
