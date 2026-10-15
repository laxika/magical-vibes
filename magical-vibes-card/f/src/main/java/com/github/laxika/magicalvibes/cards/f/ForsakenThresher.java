package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

public class ForsakenThresher extends Card {

    public ForsakenThresher() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new AwardAnyColorManaEffect());
    }
}
