package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "LCI", collectorNumber = "127")
public class TerrorTide extends Card {

    public TerrorTide() {
        Scaled minusPermanentCards = new Scaled(
                new CardsInGraveyard(new CardIsPermanentPredicate(), CountScope.CONTROLLER), -1);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(minusPermanentCards, minusPermanentCards));
    }
}
