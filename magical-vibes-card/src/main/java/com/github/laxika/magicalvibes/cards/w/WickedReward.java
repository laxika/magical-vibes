package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VIS", collectorNumber = "75")
@CardRegistration(set = "MGB", collectorNumber = "7")
public class WickedReward extends Card {

    public WickedReward() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new SacrificeCreatureCost())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 2));
    }
}
