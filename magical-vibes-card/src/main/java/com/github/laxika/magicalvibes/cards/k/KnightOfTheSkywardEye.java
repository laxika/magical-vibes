package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "15")
public class KnightOfTheSkywardEye extends Card {

    public KnightOfTheSkywardEye() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}", List.of(new BoostSelfEffect(3, 3)),
                "{3}{G}: This creature gets +3/+3 until end of turn. Activate only once each turn.", 1));
    }
}
