package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "33")
public class ConjuredCurrency extends Card {

    private static final PermanentPredicate NEITHER_OWNED_NOR_CONTROLLED = new PermanentAllOfPredicate(List.of(
            new PermanentNotPredicate(new PermanentOwnedBySourceControllerPredicate()),
            new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
    ));

    public ConjuredCurrency() {
        // At the beginning of your upkeep, you may exchange control of this enchantment and target
        // permanent you neither own nor control.
        target(new PermanentPredicateTargetFilter(NEITHER_OWNED_NOR_CONTROLLED,
                "Target must be a permanent you neither own nor control."))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new MayEffect(
                        new ExchangeControlOfTargetPermanentsEffect(NEITHER_OWNED_NOR_CONTROLLED, false, true, true),
                        "Exchange control of Conjured Currency and target permanent you neither own nor control?"
                ));
    }
}
