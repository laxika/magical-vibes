package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "506")
public class HawkeyeTrickShot extends Card {

    public HawkeyeTrickShot() {
        // Whenever Hawkeye or another Hero you control enters, it deals damage equal to the
        // number of Heroes you control to any target.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, heroDamage());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.HERO), heroDamage()));
    }

    private static DealDamageToAnyTargetEffect heroDamage() {
        return new DealDamageToAnyTargetEffect(new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.HERO), CountScope.CONTROLLER));
    }
}
