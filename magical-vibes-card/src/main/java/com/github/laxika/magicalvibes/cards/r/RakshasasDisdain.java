package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;

@CardRegistration(set = "FRF", collectorNumber = "45")
public class RakshasasDisdain extends Card {

    public RakshasasDisdain() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(
                new CardsInGraveyard(null, CountScope.CONTROLLER)));
    }
}
