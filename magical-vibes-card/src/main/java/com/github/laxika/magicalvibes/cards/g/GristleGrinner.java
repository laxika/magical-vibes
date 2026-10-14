package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "CSP", collectorNumber = "59")
@CardRegistration(set = "CST", collectorNumber = "59")
public class GristleGrinner extends Card {

    public GristleGrinner() {
        // Whenever a creature dies, this creature gets +2/+2 until end of turn. Other creatures are
        // handled by ON_ANY_CREATURE_DIES; this creature's own death is handled by ON_DEATH.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new BoostSelfEffect(2, 2));
        addEffect(EffectSlot.ON_DEATH, new BoostSelfEffect(2, 2));
    }
}
