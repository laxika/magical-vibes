package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostBySharedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OHOP", collectorNumber = "40")
public class VelisVel extends Card {

    public VelisVel() {
        addEffect(EffectSlot.STATIC, new BoostBySharedCreatureTypeEffect());
        target(TargetFilters.creature()).addEffect(
                EffectSlot.CHAOS_TRIGGERED,
                new GrantKeywordEffect(Keyword.CHANGELING, GrantScope.TARGET));
    }
}
