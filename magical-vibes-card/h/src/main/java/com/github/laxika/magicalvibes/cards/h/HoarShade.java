package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "131")
public class HoarShade extends Card {

    public HoarShade() {
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(BoostEffect.self(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."));
    }
}
