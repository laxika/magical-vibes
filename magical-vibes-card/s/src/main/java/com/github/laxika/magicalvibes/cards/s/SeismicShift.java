package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "141")
public class SeismicShift extends Card {

    public SeismicShift() {
        setAllowSharedTargets(true);

        // Destroy target land
        target(TargetFilters.land()).addSpellEffect(new DestroyTargetPermanentEffect());

        // Up to two target creatures can't block this turn
        target(TargetFilters.creature(), 0, 2).addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
