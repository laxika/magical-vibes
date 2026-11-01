package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "206")
@CardRegistration(set = "FIN", collectorNumber = "391")
@CardRegistration(set = "FIN", collectorNumber = "473")
@CardRegistration(set = "FIN", collectorNumber = "536")
@CardRegistration(set = "FIN", collectorNumber = "567")
public class TifaLockhart extends Card {

    public TifaLockhart() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new BoostSelfEffect(new SourcePower(true), new Fixed(0)));
    }
}
