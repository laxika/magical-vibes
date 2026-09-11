package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "47")
public class SlipThroughSpace extends Card {

    public SlipThroughSpace() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
