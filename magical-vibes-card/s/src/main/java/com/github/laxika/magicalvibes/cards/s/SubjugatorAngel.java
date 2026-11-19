package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "43")
@CardRegistration(set = "INR", collectorNumber = "350")
@CardRegistration(set = "EMN", collectorNumber = "45")
@CardRegistration(set = "GNT", collectorNumber = "17")
@CardRegistration(set = "SIR", collectorNumber = "48")
public class SubjugatorAngel extends Card {

    public SubjugatorAngel() {
        // When this creature enters, tap all creatures your opponents control.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(
                Subjects.controlledBy(Subjects.eachPlayer(),
                        new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))))));
    }
}
