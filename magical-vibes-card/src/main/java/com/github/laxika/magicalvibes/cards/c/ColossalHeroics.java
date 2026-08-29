package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "118")
public class ColossalHeroics extends Card {

    public ColossalHeroics() {
        setAdditionalManaCostPerExtraTarget("{1}{G}");
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2))
                .addEffect(EffectSlot.SPELL, new UntapPermanentsEffect(TapUntapScope.ALL_TARGETS));
    }
}
