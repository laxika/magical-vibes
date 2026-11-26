package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "82")
public class BlisteringDieflyn extends Card {

    public BlisteringDieflyn() {
        addActivatedAbility(new ActivatedAbility(false, "{B/R}", List.of(BoostEffect.self(1, 0)), "{B/R}: This creature gets +1/+0 until end of turn."));
    }
}
