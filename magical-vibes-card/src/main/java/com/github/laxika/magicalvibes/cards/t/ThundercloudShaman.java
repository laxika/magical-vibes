package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "195")
@CardRegistration(set = "MMA", collectorNumber = "135")
@CardRegistration(set = "C15", collectorNumber = "168")
@CardRegistration(set = "CM2", collectorNumber = "123")
public class ThundercloudShaman extends Card {

    public ThundercloudShaman() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToEachMatchingPermanentEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.GIANT), CountScope.CONTROLLER),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.GIANT)))),
                EachPermanentScope.ALL_PLAYERS));
    }
}
