package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "209")
@CardRegistration(set = "GNT", collectorNumber = "51")
@CardRegistration(set = "2XM", collectorNumber = "250")
@CardRegistration(set = "KLR", collectorNumber = "236")
public class EagerConstruct extends Card {

    public EagerConstruct() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachPlayerMayScryEffect(1));
    }
}
