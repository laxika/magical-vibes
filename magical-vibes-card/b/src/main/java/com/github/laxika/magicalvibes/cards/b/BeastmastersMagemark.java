package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CreaturesBlockingSource;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GPT", collectorNumber = "80")
public class BeastmastersMagemark extends Card {

    public BeastmastersMagemark() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(),
                        new PermanentIsEnchantedPredicate()))
                .addTrigger(Triggers.creatureYouControlBecomesBlocked()
                                .withFilter(new PermanentIsEnchantedPredicate()),
                        new BoostSelfEffect(Subjects.triggering(), new CreaturesBlockingSource(true), new CreaturesBlockingSource(true)));
    }
}
