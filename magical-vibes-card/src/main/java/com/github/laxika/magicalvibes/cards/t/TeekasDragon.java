package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "MIR", collectorNumber = "320")
public class TeekasDragon extends Card {

    public TeekasDragon() {
        // Rampage 4: whenever Teeka's Dragon becomes blocked, it gets +4/+4 until end of
        // turn for each creature blocking it beyond the first, i.e. 4 * (blockers - 1).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 4),
                new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 4)));
    }
}
