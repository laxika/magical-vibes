package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "24")
@CardRegistration(set = "DBL", collectorNumber = "291")
public class MilitiaRallier extends Card {

    public MilitiaRallier() {
        addStaticEffect(new CantAttackOrBlockAloneEffect(false));

        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(),
                        new UntapPermanentsEffect(TapUntapScope.TARGET, new PermanentIsCreaturePredicate()));
    }
}
