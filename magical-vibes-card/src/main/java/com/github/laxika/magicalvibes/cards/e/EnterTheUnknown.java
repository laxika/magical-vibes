package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.PlayAdditionalLandsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "128")
public class EnterTheUnknown extends Card {

    public EnterTheUnknown() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new ExploreEffect(true));
        addEffect(EffectSlot.SPELL, new PlayAdditionalLandsEffect(1));
    }
}
