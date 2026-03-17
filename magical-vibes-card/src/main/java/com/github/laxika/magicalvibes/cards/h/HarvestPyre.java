package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "146")
public class HarvestPyre extends Card {

    public HarvestPyre() {
        addEffect(EffectSlot.SPELL, new ExileXCardsFromGraveyardCost());
        addEffect(EffectSlot.SPELL, new DealXDamageToTargetCreatureEffect());
    }
}
