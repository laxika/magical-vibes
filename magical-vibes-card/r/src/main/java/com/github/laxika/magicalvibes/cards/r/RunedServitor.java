package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "238")
@CardRegistration(set = "ROE", collectorNumber = "224")
@CardRegistration(set = "DDF", collectorNumber = "42")
@CardRegistration(set = "MM2", collectorNumber = "226")
@CardRegistration(set = "DDP", collectorNumber = "66")
@CardRegistration(set = "IMA", collectorNumber = "226")
public class RunedServitor extends Card {

    public RunedServitor() {
        // "When this creature dies, each player draws a card."
        addTrigger(Triggers.dies(ObjectRelation.THIS), new EachPlayerDrawsCardEffect(1));
    }
}
