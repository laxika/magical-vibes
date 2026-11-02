package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INR", collectorNumber = "43")
@CardRegistration(set = "INR", collectorNumber = "350")
@CardRegistration(set = "EMN", collectorNumber = "45")
@CardRegistration(set = "GNT", collectorNumber = "17")
@CardRegistration(set = "SIR", collectorNumber = "48")
public class SubjugatorAngel extends Card {

    public SubjugatorAngel() {
        // When this creature enters, tap all creatures your opponents control.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
