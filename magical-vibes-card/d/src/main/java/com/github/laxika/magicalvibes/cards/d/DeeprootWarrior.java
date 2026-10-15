package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "XLN", collectorNumber = "186")
public class DeeprootWarrior extends Card {

    public DeeprootWarrior() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(1, 1));
    }
}
