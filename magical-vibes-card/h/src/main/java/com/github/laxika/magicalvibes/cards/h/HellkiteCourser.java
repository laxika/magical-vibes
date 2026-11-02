package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCommanderFromCommandZoneOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "218")
public class HellkiteCourser extends Card {

    public HellkiteCourser() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new PutCommanderFromCommandZoneOntoBattlefieldEffect(),
                "Put a commander you own from the command zone onto the battlefield?"));
    }
}
