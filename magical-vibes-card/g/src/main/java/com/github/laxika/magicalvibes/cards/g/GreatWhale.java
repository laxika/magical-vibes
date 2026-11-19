package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "77")
public class GreatWhale extends Card {

    public GreatWhale() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new UntapPermanentsEffect(Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentIsLandPredicate()), null, 7));
    }
}
