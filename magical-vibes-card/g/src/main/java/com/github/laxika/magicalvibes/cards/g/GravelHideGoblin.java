package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "105")
public class GravelHideGoblin extends Card {

    public GravelHideGoblin() {
        addActivatedAbility(new ActivatedAbility(false, "{3}{G}", List.of(new BoostSelfEffect(2, 2)),
                "{3}{G}: This creature gets +2/+2 until end of turn."));
    }
}
