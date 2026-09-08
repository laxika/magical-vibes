package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "CHR", collectorNumber = "79")
public class MarhaultElsdragon extends Card {

    public MarhaultElsdragon() {
        // Rampage 1: whenever this creature becomes blocked, it gets +1/+1 until end of
        // turn for each creature blocking it beyond the first, i.e. blockers - 1.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Sum(new CreaturesBlockingSource(), new Fixed(-1)),
                new Sum(new CreaturesBlockingSource(), new Fixed(-1))));
    }
}
