package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "109")
public class TouchOfInvisibility extends Card {

    public TouchOfInvisibility() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
