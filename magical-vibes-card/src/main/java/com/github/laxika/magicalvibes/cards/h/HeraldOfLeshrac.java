package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "62")
public class HeraldOfLeshrac extends Card {

    public HeraldOfLeshrac() {
        PermanentAllOfPredicate landsYouControlButDoNotOwn = new PermanentAllOfPredicate(List.of(
                new PermanentIsLandPredicate(),
                new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate())));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                CumulativeUpkeepEffect.gainControlOf(new PermanentIsLandPredicate()));
        addEffect(EffectSlot.STATIC, new DynamicStaticBoostEffect(
                new PermanentCount(landsYouControlButDoNotOwn, CountScope.CONTROLLER),
                new PermanentCount(landsYouControlButDoNotOwn, CountScope.CONTROLLER), GrantScope.SELF));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new EachPlayerGainsControlOfOwnedPermanentsMatchingEffect(new PermanentIsLandPredicate()));
    }
}
