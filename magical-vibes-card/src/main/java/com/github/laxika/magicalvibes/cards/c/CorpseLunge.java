package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "93")
public class CorpseLunge extends Card {

    public CorpseLunge() {
        // The exile cost snapshots the exiled creature card's power into the entry's xValue.
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE, false, false, true));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(new XValue()));
    }
}
