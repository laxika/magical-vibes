package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "191")
public class Phytotitan extends Card {

    public Phytotitan() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), RegisterDelayedSelfReturnFromGraveyardEffect.tappedAtOwnersNextUpkeep());
    }
}
