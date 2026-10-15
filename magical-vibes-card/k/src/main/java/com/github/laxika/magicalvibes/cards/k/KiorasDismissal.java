package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "44")
public class KiorasDismissal extends Card {

    public KiorasDismissal() {
        setAdditionalManaCostPerExtraTarget("{U}");

        target(TargetFilters.enchantment(), 0, 99)
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
    }
}
