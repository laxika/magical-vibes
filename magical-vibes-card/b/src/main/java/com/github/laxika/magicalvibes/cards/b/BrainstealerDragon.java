package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeToOwnerOfTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "OTC", collectorNumber = "127")
public class BrainstealerDragon extends Card {

    public BrainstealerDragon() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect());
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new LoseLifeToOwnerOfTriggeringPermanentEffect());
    }
}
