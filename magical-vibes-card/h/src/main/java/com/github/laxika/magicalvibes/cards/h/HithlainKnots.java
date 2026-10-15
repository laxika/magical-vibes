package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOC", collectorNumber = "176")
public class HithlainKnots extends Card {

    public HithlainKnots() {
        // Tap target creature. Scry 1. Draw a card.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, SequenceEffect.of(
                new TapPermanentsEffect(TapUntapScope.TARGET),
                new ScryEffect(1),
                new DrawCardEffect(1)
        ));
    }
}
