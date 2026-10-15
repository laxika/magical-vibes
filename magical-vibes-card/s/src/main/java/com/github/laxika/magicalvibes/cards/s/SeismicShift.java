package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "141")
public class SeismicShift extends Card {

    public SeismicShift() {
        setAllowSharedTargets(true);

        // Destroy target land
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        // Up to two target creatures can't block this turn
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
