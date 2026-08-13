package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAtEndOfCombatEffect;

@CardRegistration(set = "USG", collectorNumber = "315")
public class WallOfJunk extends Card {

    public WallOfJunk() {
        addEffect(EffectSlot.ON_BLOCK, new ReturnSelfToHandAtEndOfCombatEffect());
    }
}
