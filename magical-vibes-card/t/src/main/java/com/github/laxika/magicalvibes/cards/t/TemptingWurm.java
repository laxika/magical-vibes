package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "291")
public class TemptingWurm extends Card {

    public TemptingWurm() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                EachPlayerMayPutCardFromHandToBattlefieldEffect.temptingWurm());
    }
}
