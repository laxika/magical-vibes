package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "50")
public class EyeOfMalcator extends Card {

    public EyeOfMalcator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new AnimatePermanentsEffect(4, 4,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.EYE), Set.of()));
    }
}
