package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "91")
public class BlindingFlare extends Card {

    public BlindingFlare() {
        // Strive — This spell costs {R} more to cast for each target beyond the first.
        setAdditionalManaCostPerExtraTarget("{R}");

        // Any number of target creatures can't block this turn.
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
