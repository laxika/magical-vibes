package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "87")
public class Batterhorn extends Card {

    public Batterhorn() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target artifact?"));
    }
}
