package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "311")
public class Tanglebloom extends Card {

    public Tanglebloom() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new GainLifeEffect(1)),
                "{1}, {T}: You gain 1 life."
        ));
    }
}
