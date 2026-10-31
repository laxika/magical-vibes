package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.SourceCardPower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "ISD", collectorNumber = "93")
public class CorpseLunge extends Card {

    public CorpseLunge() {
        addCastingCost(new ExileCardFromGraveyardCost(CardType.CREATURE, false, false, true));
        addSpellEffect(new DealDamageToTargetCreatureEffect(SourceCardPower.ofExiledCostCard()));
    }
}
