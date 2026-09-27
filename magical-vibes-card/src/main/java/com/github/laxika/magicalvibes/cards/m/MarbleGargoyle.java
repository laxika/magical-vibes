package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "21")
public class MarbleGargoyle extends Card {

    public MarbleGargoyle() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostSelfEffect(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."
        ));
    }
}
