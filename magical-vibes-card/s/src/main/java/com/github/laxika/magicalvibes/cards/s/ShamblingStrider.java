package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "263")
@CardRegistration(set = "BTD", collectorNumber = "63")
@CardRegistration(set = "ME1", collectorNumber = "129")
public class ShamblingStrider extends Card {

    public ShamblingStrider() {
        addActivatedAbility(new ActivatedAbility(false, "{R}{G}", List.of(BoostEffect.self(1, -1)), "{R}{G}: This creature gets +1/-1 until end of turn."));
    }
}
