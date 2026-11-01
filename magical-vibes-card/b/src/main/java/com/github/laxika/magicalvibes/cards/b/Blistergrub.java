package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "56")
@CardRegistration(set = "DDR", collectorNumber = "40")
public class Blistergrub extends Card {

    public Blistergrub() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
    }
}
