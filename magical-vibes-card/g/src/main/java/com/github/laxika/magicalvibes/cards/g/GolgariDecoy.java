package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;

@CardRegistration(set = "RTR", collectorNumber = "127")
public class GolgariDecoy extends Card {

    public GolgariDecoy() {
        // All creatures able to block this creature do so.
        addEffect(EffectSlot.STATIC, new MustBeBlockedByAllCreaturesEffect());

        // Scavenge {3}{G}{G}
        addScavenge("{3}{G}{G}");
    }
}
