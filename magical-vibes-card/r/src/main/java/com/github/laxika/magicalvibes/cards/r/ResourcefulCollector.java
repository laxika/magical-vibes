package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeRandomGraveyardPermanentFoodEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "29")
public class ResourcefulCollector extends Card {

    public ResourcefulCollector() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new PerpetuallyMakeRandomGraveyardPermanentFoodEffect());
    }
}
