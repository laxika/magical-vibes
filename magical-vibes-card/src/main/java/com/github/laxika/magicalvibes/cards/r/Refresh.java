package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "264")
public class Refresh extends Card {

    public Refresh() {
        // Regenerate target creature. Draw a card.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RegenerateEffect(true))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
