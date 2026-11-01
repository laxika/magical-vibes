package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "188")
@CardRegistration(set = "KTK", collectorNumber = "135")
public class HighlandGame extends Card {

    public HighlandGame() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
