package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryForOwnerOfCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBLB", collectorNumber = "22")
public class DarkstarBanisher extends Card {

    public DarkstarBanisher() {
        PermanentAllOfPredicate targetPredicate = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                new PermanentMaxManaValuePredicate(4)
        ));
        target(new PermanentPredicateTargetFilter(
                targetPredicate,
                "Target must be a nonland permanent an opponent controls with mana value 4 or less"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetPermanentAndTrackWithSourceEffect());

        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new SeekLibraryForOwnerOfCardExiledWithSourceEffect());
    }
}
