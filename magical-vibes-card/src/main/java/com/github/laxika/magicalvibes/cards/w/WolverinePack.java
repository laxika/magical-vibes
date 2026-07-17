package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "5ED", collectorNumber = "344")
public class WolverinePack extends Card {

    public WolverinePack() {
        // Rampage 2: whenever Wolverine Pack becomes blocked, it gets +2/+2 until end of
        // turn for each creature blocking it beyond the first, i.e. 2 * (blockers - 1).
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 2),
                new Scaled(new Sum(new CreaturesBlockingSource(), new Fixed(-1)), 2)));
    }
}
