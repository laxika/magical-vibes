package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestToughnessAmongControlled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "199")
@CardRegistration(set = "DBL", collectorNumber = "466")
public class FlourishingHunter extends Card {

    public FlourishingHunter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new GainLifeEffect(new GreatestToughnessAmongControlled(
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))));
    }
}
