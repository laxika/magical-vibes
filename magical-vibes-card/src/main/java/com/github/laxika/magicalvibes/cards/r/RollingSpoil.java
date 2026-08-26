package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "179")
public class RollingSpoil extends Card {

    public RollingSpoil() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.BLACK),
                new BoostAllCreaturesEffect(-1, -1)));
    }
}
