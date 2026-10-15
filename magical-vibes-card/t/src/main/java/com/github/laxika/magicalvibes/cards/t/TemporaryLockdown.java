package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "36")
public class TemporaryLockdown extends Card {

    public TemporaryLockdown() {
        // When this enchantment enters, exile each nonland permanent with mana value 2 or less
        // until this enchantment leaves the battlefield.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileAllPermanentsUntilSourceLeavesEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentMaxManaValuePredicate(2)
                )),
                false));
    }
}
