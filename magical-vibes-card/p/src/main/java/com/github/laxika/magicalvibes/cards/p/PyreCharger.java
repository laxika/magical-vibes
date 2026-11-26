package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "103")
@CardRegistration(set = "DD2", collectorNumber = "38")
@CardRegistration(set = "JVC", collectorNumber = "38")
public class PyreCharger extends Card {

    public PyreCharger() {
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 0)), "{R}: This creature gets +1/+0 until end of turn."));
    }
}
