package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "143")
public class AlmightyBrushwagg extends Card {

    public AlmightyBrushwagg() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}", List.of(new BoostSelfEffect(3, 3)),
                "{3}{G}: This creature gets +3/+3 until end of turn."));
    }
}
