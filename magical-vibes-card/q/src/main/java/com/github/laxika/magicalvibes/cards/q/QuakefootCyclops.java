package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "142")
public class QuakefootCyclops extends Card {

    public QuakefootCyclops() {
        // When this creature enters, up to two target creatures can't block this turn.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET));

        // Cycling {1}{R} ({1}{R}, Discard this card: Draw a card.)
        addCycling("{1}{R}");

        // When you cycle this card, target creature can't block this turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_SELF_CYCLED,
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
