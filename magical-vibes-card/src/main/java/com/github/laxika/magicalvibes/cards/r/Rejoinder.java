package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Rejoinder, the prepare spell of Elite Interceptor // Rejoinder.
 */
public class Rejoinder extends Card {

    public Rejoinder() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new MayEffect(new TapOrUntapTargetPermanentEffect(), "Tap or untap target creature?"))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
