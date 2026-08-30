package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromTargetToAnotherTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DIS", collectorNumber = "6")
public class Carom extends Card {

    public Carom() {
        target(TargetFilters.creature());
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new RedirectNextDamageFromTargetToAnotherTargetEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
