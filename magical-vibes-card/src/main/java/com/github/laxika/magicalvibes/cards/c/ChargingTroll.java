package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "239")
public class ChargingTroll extends Card {

    public ChargingTroll() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate Charging Troll."
        ));
    }
}
