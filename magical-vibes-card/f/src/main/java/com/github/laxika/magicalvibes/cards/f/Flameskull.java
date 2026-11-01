package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "143")
public class Flameskull extends Card {

    public Flameskull() {
        addStaticEffect(new CantBlockEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect());
    }
}
