package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "150")
public class SolphimMayhemDominus extends Card {

    public SolphimMayhemDominus() {
        addEffect(EffectSlot.STATIC,
                new DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R/P}{R/P}",
                List.of(
                        new DiscardCardTypeCost(null, null, 2),
                        new PutCountersOnSelfEffect(CounterType.INDESTRUCTIBLE)
                ),
                "{1}{R/P}{R/P}, Discard two cards: Put an indestructible counter on Solphim."
        ));
    }
}
