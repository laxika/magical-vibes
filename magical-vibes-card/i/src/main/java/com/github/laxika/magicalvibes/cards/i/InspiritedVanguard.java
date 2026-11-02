package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EndureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "146")
public class InspiritedVanguard extends Card {

    public InspiritedVanguard() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EndureEffect(2));
        addTrigger(Triggers.attacks(), new EndureEffect(2));
    }
}
