package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "165")
public class DeputyOfDetention extends Card {

    public DeputyOfDetention() {
        // When this creature enters, exile target nonland permanent an opponent controls and all
        // other nonland permanents that player controls with the same name as that permanent until
        // this creature leaves the battlefield.
        PermanentPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilSourceLeavesEffect(
                                Subjects.allOf(Subjects.target(), Subjects.except(
                                        Subjects.otherPermanentsSharing(Subjects.target(), SharedQuality.NAME, nonland),
                                        Subjects.controlledBy(
                                                Subjects.opponentsOf(Subjects.controllerOf(Subjects.target())),
                                                new PermanentTruePredicate()))),
                                nonland, false, false));
    }
}
