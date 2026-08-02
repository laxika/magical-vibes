package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "37")
@CardRegistration(set = "M15", collectorNumber = "35")
public class Soulmender extends Card {

    public Soulmender() {
        // {T}: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new GainLifeEffect(1)),
                "{T}: You gain 1 life."
        ));
    }
}
