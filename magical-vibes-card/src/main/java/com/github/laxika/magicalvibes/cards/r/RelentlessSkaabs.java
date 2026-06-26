package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

@CardRegistration(set = "DKA", collectorNumber = "45")
public class RelentlessSkaabs extends Card {

    public RelentlessSkaabs() {
        addEffect(EffectSlot.SPELL, new ExileCardFromGraveyardCost(CardType.CREATURE));
    }
}
