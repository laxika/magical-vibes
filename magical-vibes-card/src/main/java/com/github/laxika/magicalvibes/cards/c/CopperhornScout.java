package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UntapEachOtherCreatureYouControlEffect;

@CardRegistration(set = "SOM", collectorNumber = "116")
public class CopperhornScout extends Card {

    public CopperhornScout() {
        addEffect(EffectSlot.ON_ATTACK, new UntapEachOtherCreatureYouControlEffect());
    }
}
