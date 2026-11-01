package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LEG", collectorNumber = "49")
public class BrineHag extends Card {

    public BrineHag() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect(0, 2));
    }
}
