package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OHOP", collectorNumber = "40")
@CardRegistration(set = "OPCA", collectorNumber = "84")
public class VelisVel extends Card {

    public VelisVel() {
        addStaticEffect(new BoostBySharedCreatureTypeEffect());
        target(TargetFilters.creature()).addEffect(
                EffectSlot.CHAOS_TRIGGERED,
                new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.TARGET));
    }
}
