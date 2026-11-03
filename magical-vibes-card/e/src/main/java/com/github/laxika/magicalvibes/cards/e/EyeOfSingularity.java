package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOtherPermanentsWithEnteringNameEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithAnotherPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "4")
public class EyeOfSingularity extends Card {

    public EyeOfSingularity() {
        // When this enchantment enters, destroy each permanent with the same name as another
        // permanent, except for basic lands. They can't be regenerated.
        // World rule is an SBA — nothing on the card.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentAllOfPredicate(List.of(
                                new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.BASIC)))),
                        new PermanentSharesNameWithAnotherPermanentPredicate())),
                true));

        // Whenever a permanent other than a basic land enters, destroy all other permanents with
        // that name. They can't be regenerated.
        addTrigger(Triggers.enters(
                        new PermanentNotPredicate(new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(),
                                new PermanentHasSupertypePredicate(CardSupertype.BASIC)))),
                        ObjectRelation.ANY),
                new DestroyOtherPermanentsWithEnteringNameEffect());
    }
}
