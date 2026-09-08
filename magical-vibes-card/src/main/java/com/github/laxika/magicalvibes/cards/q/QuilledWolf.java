package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "SOI", collectorNumber = "222")
public class QuilledWolf extends Card {

    public QuilledWolf() {
        addActivatedAbility(new ActivatedAbility(false, "{5}{G}", List.of(new BoostSelfEffect(4, 4)), "{5}{G}: This creature gets +4/+4 until end of turn."));
    }
}
