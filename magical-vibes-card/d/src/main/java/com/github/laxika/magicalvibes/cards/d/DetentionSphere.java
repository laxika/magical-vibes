package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject.OtherPermanentsSharing.SharedQuality;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "155")
@CardRegistration(set = "GK2", collectorNumber = "10")
@CardRegistration(set = "OTP", collectorNumber = "43")
public class DetentionSphere extends Card {

    public DetentionSphere() {
        // When this enchantment enters, you may exile target nonland permanent not named
        // Detention Sphere and all other permanents with the same name as that permanent.
        // When this enchantment leaves the battlefield, return the exiled cards to the
        // battlefield under their owner's control (one leaves-the-battlefield ability; if it
        // resolves first, the enter ability's exile is indefinite, per the card's ruling).
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentNamedPredicate("Detention Sphere"))
                )),
                "Target must be a nonland permanent not named Detention Sphere"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new MayEffect(
                                new ExileTargetPermanentUntilSourceLeavesEffect(
                                        Subjects.allOf(Subjects.target(), Subjects.otherPermanentsSharing(
                                                Subjects.target(), SharedQuality.NAME, new PermanentTruePredicate())),
                                        new PermanentNotPredicate(new PermanentIsLandPredicate()), true, true),
                                "Exile target nonland permanent and all with the same name?"));
    }
}
