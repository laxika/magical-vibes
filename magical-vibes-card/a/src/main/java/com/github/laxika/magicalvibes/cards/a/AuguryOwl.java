package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "45")
@CardRegistration(set = "DDI", collectorNumber = "3")
@CardRegistration(set = "PC2", collectorNumber = "14")
@CardRegistration(set = "PCA", collectorNumber = "14")
public class AuguryOwl extends Card {

    public AuguryOwl() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(3));
    }
}
