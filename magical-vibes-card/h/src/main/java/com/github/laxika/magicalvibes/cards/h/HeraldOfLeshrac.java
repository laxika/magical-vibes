package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerGainsControlOfOwnedPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "62")
public class HeraldOfLeshrac extends Card {

    public HeraldOfLeshrac() {
        PermanentAllOfPredicate landsYouControlButDoNotOwn = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                CumulativeUpkeepEffect.gainControlOf(new PermanentIsLandPredicate()));
        addStaticEffect(new DynamicStaticBoostEffect(
                new PermanentCount(landsYouControlButDoNotOwn, CountScope.CONTROLLER),
                new PermanentCount(landsYouControlButDoNotOwn, CountScope.CONTROLLER), GrantScope.SELF));
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(), new PermanentControlledBySourceControllerPredicate()))));
    }
}
