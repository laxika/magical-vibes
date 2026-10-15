package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "190")
@CardRegistration(set = "AKR", collectorNumber = "222")
public class SynchronizedStrike extends Card {

    public SynchronizedStrike() {
        // Untap up to two target creatures. They each get +2/+2 until end of turn.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS))
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
    }
}
