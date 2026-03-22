package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;

@CardRegistration(set = "DOM", collectorNumber = "188")
public class WildOnslaught extends Card {

    public WildOnslaught() {
        // Kicker {4}
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));

        // Put a +1/+1 counter on each creature you control.
        // If this spell was kicked, put two +1/+1 counters on each creature you control instead.
        addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect(1),
                new PutPlusOnePlusOneCounterOnEachOwnCreatureEffect(2)
        ));
    }
}
