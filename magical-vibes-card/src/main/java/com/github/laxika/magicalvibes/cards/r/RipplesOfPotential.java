package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsThatReceivedCountersThisWayEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "SLD", collectorNumber = "2453")
public class RipplesOfPotential extends Card {

    public RipplesOfPotential() {
        addEffect(EffectSlot.SPELL, new ProliferateEffect());
        addEffect(EffectSlot.SPELL, new PhaseOutPermanentsThatReceivedCountersThisWayEffect());
    }
}
