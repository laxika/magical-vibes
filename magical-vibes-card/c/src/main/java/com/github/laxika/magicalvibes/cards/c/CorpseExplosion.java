package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "SNC", collectorNumber = "179")
public class CorpseExplosion extends Card {

    public CorpseExplosion() {
        addCastingCost(new ExileCardFromGraveyardCost(CardType.CREATURE, false, false, true));
        addSpellEffect(DealDamageEffect.toEachCreatureAndPlaneswalker(new XValue()));
    }
}
