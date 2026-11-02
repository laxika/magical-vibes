package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "103")
@CardRegistration(set = "EMA", collectorNumber = "154")
@CardRegistration(set = "VMA", collectorNumber = "193")
@CardRegistration(set = "DMR", collectorNumber = "148")
public class WorldgorgerDragon extends Card {

    public WorldgorgerDragon() {
        PermanentPredicate otherPermanentsYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileAllPermanentsUntilSourceLeavesEffect(otherPermanentsYouControl, false, false, true));
    }
}
