package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "247")
public class FriendlyTeddy extends Card {

    public FriendlyTeddy() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new EachPlayerDrawsCardEffect(1));
    }
}
