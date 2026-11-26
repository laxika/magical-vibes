package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "58")
public class HermiticNautilus extends Card {

    public HermiticNautilus() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{U}", List.of(BoostEffect.self(3, -3)),
                "{1}{U}: This creature gets +3/-3 until end of turn."));
    }
}
