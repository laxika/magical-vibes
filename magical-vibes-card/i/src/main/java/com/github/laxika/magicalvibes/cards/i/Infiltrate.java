package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEM", collectorNumber = "33")
public class Infiltrate extends Card {

    public Infiltrate() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
    }
}
