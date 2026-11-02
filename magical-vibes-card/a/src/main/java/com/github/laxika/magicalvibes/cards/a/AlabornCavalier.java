package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "P02", collectorNumber = "1")
@CardRegistration(set = "DDG", collectorNumber = "18")
public class AlabornCavalier extends Card {

    public AlabornCavalier() {
        // Whenever this creature attacks, you may tap target creature.
        target(TargetFilters.creature()).addTrigger(Triggers.attacks(), new MayEffect(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                "Tap target creature?"
        ));
    }
}
