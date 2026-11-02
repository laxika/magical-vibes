package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "47")
@CardRegistration(set = "2XM", collectorNumber = "45")
public class CloudreaderSphinx extends Card {

    public CloudreaderSphinx() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
    }
}
