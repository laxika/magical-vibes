package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "INV", collectorNumber = "312")
public class SparringGolem extends Card {

    public SparringGolem() {
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new CreaturesBlockingSource(),
                new CreaturesBlockingSource()));
    }
}
