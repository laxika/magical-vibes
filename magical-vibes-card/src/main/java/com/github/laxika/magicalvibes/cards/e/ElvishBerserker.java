package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "10E", collectorNumber = "260")
@CardRegistration(set = "9ED", collectorNumber = "237")
public class ElvishBerserker extends Card {

    public ElvishBerserker() {
        // Whenever Elvish Berserker becomes blocked, it gets +1/+1 until end of turn
        // for each creature blocking it.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(
                new CreaturesBlockingSource(), new CreaturesBlockingSource()));
    }
}
