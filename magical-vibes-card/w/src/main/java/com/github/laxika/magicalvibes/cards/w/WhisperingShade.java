package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "167")
public class WhisperingShade extends Card {

    public WhisperingShade() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(BoostEffect.self(1, 1)),
                "{B}: This creature gets +1/+1 until end of turn."
        ));
    }
}
