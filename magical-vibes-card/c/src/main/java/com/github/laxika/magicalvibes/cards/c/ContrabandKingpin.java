package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KLD", collectorNumber = "177")
@CardRegistration(set = "KLR", collectorNumber = "190")
public class ContrabandKingpin extends Card {

    public ContrabandKingpin() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL), new ScryEffect(1));
    }
}
