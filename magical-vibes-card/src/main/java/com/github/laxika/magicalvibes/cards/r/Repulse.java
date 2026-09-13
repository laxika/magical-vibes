package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "70")
@CardRegistration(set = "DD2", collectorNumber = "25")
@CardRegistration(set = "JVC", collectorNumber = "25")
public class Repulse extends Card {

    public Repulse() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target())
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
