package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "146")
public class HarvestPyre extends Card {

    public HarvestPyre() {
        // The exile cost snapshots the number of cards exiled into the entry's xValue.
        addEffect(EffectSlot.SPELL, new ExileXCardsFromGraveyardCost());
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()));
    }
}
