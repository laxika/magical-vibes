package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "188")
@CardRegistration(set = "BRO", collectorNumber = "187")
@CardRegistration(set = "IMA", collectorNumber = "179")
public class ObstinateBaloth extends Card {

    public ObstinateBaloth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
        addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), new EnterBattlefieldOnDiscardEffect());
    }
}
