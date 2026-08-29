package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "90")
public class DrivnodCarnageDominus extends Card {

    public DrivnodCarnageDominus() {
        addEffect(EffectSlot.STATIC,
                new AdditionalCreatureDeathTriggerEffect(new PermanentTruePredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B/P}{B/P}",
                List.of(
                        new ExileNCardsFromGraveyardCost(3, CardType.CREATURE),
                        new PutCountersOnSelfEffect(CounterType.INDESTRUCTIBLE)
                ),
                "{B/P}{B/P}, Exile three creature cards from your graveyard: Put an indestructible counter on Drivnod."
        ));
    }
}
