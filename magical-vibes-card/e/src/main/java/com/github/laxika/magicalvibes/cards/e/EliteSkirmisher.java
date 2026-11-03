package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "8")
public class EliteSkirmisher extends Card {

    public EliteSkirmisher() {
        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target creature?"));
    }
}
