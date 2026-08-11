package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "THS", collectorNumber = "88")
public class FleshmadSteed extends Card {

    public FleshmadSteed() {
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TapPermanentsEffect(TapUntapScope.SELF));
    }
}
