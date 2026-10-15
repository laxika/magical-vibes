package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "227")
public class WaterWhip extends Card {

    public WaterWhip() {
        // As an additional cost to cast this spell, waterbend {5}.
        addEffect(EffectSlot.SPELL, new WaterbendCost(5));

        // Return up to two target creatures to their owners' hands.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
