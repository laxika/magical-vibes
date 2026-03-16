package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "93")
public class CorpseLunge extends Card {

    public CorpseLunge() {
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE, false, false, true));
        addEffect(EffectSlot.SPELL, new DealXDamageToTargetCreatureEffect());
    }
}
