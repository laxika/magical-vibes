package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "216")
public class GrowingDread extends Card {

    public GrowingDread() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ManifestDreadEffect.forController());
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL),
                new PutCounterOnTargetPermanentEffect(
                        Subjects.triggering(), CounterType.PLUS_ONE_PLUS_ONE));
    }
}
