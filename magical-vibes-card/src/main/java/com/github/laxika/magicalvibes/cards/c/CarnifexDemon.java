package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "57")
public class CarnifexDemon extends Card {

    public CarnifexDemon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSourceEffect(-1, -1, 2));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new RemoveCounterFromSourceCost(),
                        new PutMinusOneMinusOneCounterOnEachOtherCreatureEffect()
                ),
                "{B}, Remove a -1/-1 counter from Carnifex Demon: Put a -1/-1 counter on each other creature."
        ));
    }
}
