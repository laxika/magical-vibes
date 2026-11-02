package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "71")
@CardRegistration(set = "M15", collectorNumber = "106")
public class NecrogenScudder extends Card {

    public NecrogenScudder() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new LoseLifeEffect(3));
    }
}
