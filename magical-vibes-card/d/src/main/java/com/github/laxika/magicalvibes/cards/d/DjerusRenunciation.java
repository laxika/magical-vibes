package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;


@CardRegistration(set = "HOU", collectorNumber = "11")
public class DjerusRenunciation extends Card {

    public DjerusRenunciation() {
        // Tap up to two target creatures.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new TapPermanentsEffect(TapUntapScope.TARGET));

        // Cycling {W} ({W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{W}");
    }
}
