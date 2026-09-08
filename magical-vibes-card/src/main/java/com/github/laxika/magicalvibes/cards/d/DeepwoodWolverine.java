package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MMQ", collectorNumber = "242")
public class DeepwoodWolverine extends Card {

    public DeepwoodWolverine() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(2, 0));
    }
}
