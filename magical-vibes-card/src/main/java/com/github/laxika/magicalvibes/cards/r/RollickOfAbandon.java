package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "JOU", collectorNumber = "108")
public class RollickOfAbandon extends Card {

    public RollickOfAbandon() {
        // All creatures get +2/-2 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, -2));
    }
}
