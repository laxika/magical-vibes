package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CON", collectorNumber = "73")
@CardRegistration(set = "MM2", collectorNumber = "133")
public class ViashinoSlaughtermaster extends Card {

    public ViashinoSlaughtermaster() {
        // Double strike is auto-loaded from Scryfall.
        addActivatedAbility(new ActivatedAbility(false, "{B}{G}", List.of(BoostEffect.self(1, 1)),
                "{B}{G}: This creature gets +1/+1 until end of turn. Activate only once each turn.", 1));
    }
}
