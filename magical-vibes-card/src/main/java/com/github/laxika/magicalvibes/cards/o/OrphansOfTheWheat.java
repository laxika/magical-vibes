package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapCreaturesBoostSelfEffect;

@CardRegistration(set = "DSK", collectorNumber = "22")
public class OrphansOfTheWheat extends Card {

    public OrphansOfTheWheat() {
        addEffect(EffectSlot.ON_ATTACK, new TapCreaturesBoostSelfEffect());
    }
}
