package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "278")
@CardRegistration(set = "M19", collectorNumber = "295")
@CardRegistration(set = "GN2", collectorNumber = "34")
@CardRegistration(set = "GNT", collectorNumber = "34")
public class TatteredMummy extends Card {

    public TatteredMummy() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT));
    }
}
