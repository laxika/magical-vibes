package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "88")
@CardRegistration(set = "ORI", collectorNumber = "132")
public class BellowsLizard extends Card {

    public BellowsLizard() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{R}", List.of(BoostEffect.self(1, 0)), "{1}{R}: This creature gets +1/+0 until end of turn."));
    }
}
