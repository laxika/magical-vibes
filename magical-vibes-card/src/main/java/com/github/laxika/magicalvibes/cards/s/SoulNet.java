package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "7ED", collectorNumber = "317")
@CardRegistration(set = "6ED", collectorNumber = "313")
public class SoulNet extends Card {

    public SoulNet() {
        // Whenever a creature dies, you may pay {1}. If you do, you gain 1 life.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new MayPayManaEffect("{1}", new GainLifeEffect(1), "Pay {1} to gain 1 life?"));
    }
}
