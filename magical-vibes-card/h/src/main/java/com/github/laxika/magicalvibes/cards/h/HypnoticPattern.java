package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "34")
public class HypnoticPattern extends Card {

    public HypnoticPattern() {
        setAllowSharedTargets(true);
        target(TargetFilters.creature())
                .addSpellEffect(new PerpetuallyBoostTargetCreatureEffect(-2, 0));
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-2, 0));
    }
}
