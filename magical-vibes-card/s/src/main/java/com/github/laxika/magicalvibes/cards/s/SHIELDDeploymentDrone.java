package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "73")
public class SHIELDDeploymentDrone extends Card {

    public SHIELDDeploymentDrone() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(1));
    }
}
