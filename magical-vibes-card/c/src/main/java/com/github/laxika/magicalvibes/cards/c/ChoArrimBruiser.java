package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "9")
public class ChoArrimBruiser extends Card {

    public ChoArrimBruiser() {
        // Whenever this creature attacks, you may tap up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addTrigger(Triggers.attacks(),
                        new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET),
                                "Tap up to two target creatures?"));
    }
}
