package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeRandomGraveyardPermanentFoodEffect;

@CardRegistration(set = "YBLB", collectorNumber = "29")
public class ResourcefulCollector extends Card {

    public ResourcefulCollector() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PerpetuallyMakeRandomGraveyardPermanentFoodEffect());
    }
}
