package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BlockedByMinCreaturesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DOM", collectorNumber = "139")
public class RampagingCyclops extends Card {

    public RampagingCyclops() {
        // Rampaging Cyclops gets -2/-0 as long as two or more creatures are blocking it.
        addEffect(EffectSlot.STATIC, new BlockedByMinCreaturesConditionalEffect(2,
                new StaticBoostEffect(-2, 0, GrantScope.SELF)));
    }
}
