package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "DMC", collectorNumber = "31")
@CardRegistration(set = "DMC", collectorNumber = "53")
public class GeneralMarhaultElsdragon extends Card {

    public GeneralMarhaultElsdragon() {
        // Whenever a creature you control becomes blocked, it gets +3/+3 until end of turn
        // for each creature blocking it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED, new BoostSelfEffect(
                new Scaled(new CreaturesBlockingSource(), 3),
                new Scaled(new CreaturesBlockingSource(), 3)));
    }
}
