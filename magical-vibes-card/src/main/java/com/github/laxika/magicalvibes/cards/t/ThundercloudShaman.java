package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "LRW", collectorNumber = "195")
public class ThundercloudShaman extends Card {

    public ThundercloudShaman() {
        // When this creature enters, it deals damage equal to the number of Giants you control
        // to each non-Giant creature. (The handler already restricts targets to creatures.)
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToEachMatchingPermanentEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GIANT), CountScope.CONTROLLER),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GIANT)),
                EachPermanentScope.ALL_PLAYERS));
    }
}
