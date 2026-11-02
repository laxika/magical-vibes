package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMayTakeDamageSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TOR", collectorNumber = "103")
public class LonghornFirebeast extends Card {

    public LonghornFirebeast() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new AnyOpponentMayTakeDamageSacrificeSourceEffect(5));
    }
}
