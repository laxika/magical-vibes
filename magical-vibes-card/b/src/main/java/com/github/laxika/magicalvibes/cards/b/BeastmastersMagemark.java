package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.BoostReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "80")
public class BeastmastersMagemark extends Card {

    public BeastmastersMagemark() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                        new PermanentIsEnchantedPredicate()))
                .addTrigger(Triggers.creatureYouControlBecomesBlocked()
                                .withFilter(new PermanentIsEnchantedPredicate()),
                        new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING,
                                new CreaturesBlockingSource(true), new CreaturesBlockingSource(true)));
    }
}
