package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;

@CardRegistration(set = "WTH", collectorNumber = "71")
public class HauntingMisery extends Card {

    public HauntingMisery() {
        // The exile cost snapshots the number of creature cards exiled into the entry's xValue.
        addEffect(EffectSlot.SPELL, new ExileXCardsFromGraveyardCost(CardType.CREATURE));
        addEffect(EffectSlot.SPELL, new DealDamageToTargetPlayerOrPlaneswalkerEffect(new XValue()));
    }
}
