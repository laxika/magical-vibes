package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PowerBoostForCrewAndSaddleEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YNEO", collectorNumber = "1")
public class DragonflyPilot extends Card {

    public DragonflyPilot() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureCardNamedIntoHandEffect("NEO", "9", false));
        addStaticEffect(new PowerBoostForCrewAndSaddleEffect(2, true));
    }
}
