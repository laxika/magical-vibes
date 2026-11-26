package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EMN", collectorNumber = "103")
@CardRegistration(set = "SIR", collectorNumber = "132")
public class RuthlessDisposal extends Card {

    public RuthlessDisposal() {
        addCastingCost(new DiscardCardTypeCost(null, null));
        addCastingCost(new SacrificeCreatureCost());

        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(-13, -13));
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(-13, -13));
    }
}
