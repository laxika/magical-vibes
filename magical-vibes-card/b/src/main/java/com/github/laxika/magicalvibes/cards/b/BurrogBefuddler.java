package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "38")
@CardRegistration(set = "FDN", collectorNumber = "504")
public class BurrogBefuddler extends Card {

    public BurrogBefuddler() {
        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                BoostEffect.target(-1, 0));
    }
}
