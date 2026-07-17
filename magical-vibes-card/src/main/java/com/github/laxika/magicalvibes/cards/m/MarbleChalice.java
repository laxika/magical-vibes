package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "18")
public class MarbleChalice extends Card {

    public MarbleChalice() {
        // {T}: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(new GainLifeEffect(1)),
                "{T}: You gain 1 life."
        ));
    }
}
