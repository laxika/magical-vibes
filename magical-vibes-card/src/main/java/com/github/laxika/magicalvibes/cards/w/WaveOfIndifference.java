package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "243")
public class WaveOfIndifference extends Card {

    public WaveOfIndifference() {
        targetExactlyX(TargetFilters.creature(), 100)
                .addEffect(EffectSlot.SPELL, new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
