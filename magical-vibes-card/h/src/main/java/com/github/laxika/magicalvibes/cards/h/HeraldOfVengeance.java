package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithPermanentThatDealtDamageToSourceControllerLastTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSNC", collectorNumber = "2")
public class HeraldOfVengeance extends Card {

    public HeraldOfVengeance() {
        // Destroy each permanent you don't control that shares a name with a permanent that dealt
        // damage to you last turn.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PermanentSharesNameWithPermanentThatDealtDamageToSourceControllerLastTurnPredicate()
                ))
        ));
    }
}
