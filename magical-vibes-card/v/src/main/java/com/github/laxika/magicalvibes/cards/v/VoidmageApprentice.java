package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LGN", collectorNumber = "54")
@CardRegistration(set = "DD2", collectorNumber = "4")
@CardRegistration(set = "JVC", collectorNumber = "4")
public class VoidmageApprentice extends Card {

    public VoidmageApprentice() {
        addMorph("{2}{U}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new CounterSpellEffect());
    }
}
