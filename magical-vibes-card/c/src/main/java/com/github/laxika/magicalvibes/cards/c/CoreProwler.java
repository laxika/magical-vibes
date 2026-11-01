package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MBS", collectorNumber = "103")
public class CoreProwler extends Card {

    public CoreProwler() {
        // When Core Prowler dies, proliferate.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ProliferateEffect());
    }
}
