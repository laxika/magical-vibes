package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "143")
public class FesteringGoblin extends Card {

    public FesteringGoblin() {
        addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-1, -1));
    }
}
