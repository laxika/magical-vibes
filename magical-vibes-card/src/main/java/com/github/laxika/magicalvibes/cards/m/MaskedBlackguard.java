package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "113")
public class MaskedBlackguard extends Card {

    public MaskedBlackguard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new BoostSelfEffect(1, 1)),
                "{2}{B}: This creature gets +1/+1 until end of turn."
        ));
    }
}
