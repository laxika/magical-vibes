package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MMQ", collectorNumber = "9")
public class ChoArrimBruiser extends Card {

    public ChoArrimBruiser() {
        // Whenever this creature attacks, you may tap up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_ATTACK,
                        new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET),
                                "Tap up to two target creatures?"));
    }
}
