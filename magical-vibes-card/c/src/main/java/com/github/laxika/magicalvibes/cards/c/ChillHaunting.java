package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;

@CardRegistration(set = "SCG", collectorNumber = "60")
public class ChillHaunting extends Card {

    public ChillHaunting() {
        addCastingCost(new ExileXCardsFromGraveyardCost(CardType.CREATURE));
        addSpellEffect(new BoostTargetCreatureEffect(
                new Scaled(new XValue(), -1),
                new Scaled(new XValue(), -1)
        ));
    }
}
