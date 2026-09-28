package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesOfChosenTypeEffect;

@CardRegistration(set = "MSC", collectorNumber = "156")
@CardRegistration(set = "MSC", collectorNumber = "351")
@CardRegistration(set = "CMM", collectorNumber = "169")
@CardRegistration(set = "CMM", collectorNumber = "515")
@CardRegistration(set = "CMM", collectorNumber = "640")
public class KindredDominance extends Card {

    public KindredDominance() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesOfChosenTypeEffect(true));
    }
}
