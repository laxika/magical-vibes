package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "7ED", collectorNumber = "247")
public class GangOfElk extends Card {

    public GangOfElk() {
        // Whenever Gang of Elk becomes blocked, it gets +2/+2 until end of turn
        // for each creature blocking it.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(new CreaturesBlockingSource(), 2),
                new Scaled(new CreaturesBlockingSource(), 2)));
    }
}
