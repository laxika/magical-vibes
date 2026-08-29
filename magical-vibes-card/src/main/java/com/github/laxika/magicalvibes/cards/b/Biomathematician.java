package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "164")
public class Biomathematician extends Card {

    public Biomathematician() {
        // When this creature enters, create a 0/0 green and blue Fractal creature token.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Fractal", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL)));

        // Put a +1/+1 counter on each Fractal you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachControlledPermanentEffect(
                CounterType.PLUS_ONE_PLUS_ONE, 1,
                new PermanentHasSubtypePredicate(CardSubtype.FRACTAL)));
    }
}
