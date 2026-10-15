package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;

@CardRegistration(set = "SOM", collectorNumber = "116")
public class CopperhornScout extends Card {

    public CopperhornScout() {
        addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES));
    }
}
