package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "268")
public class NantukoDisciple extends Card {

    public NantukoDisciple() {
        addActivatedAbility(new ActivatedAbility(true, "{G}", List.of(new BoostTargetCreatureEffect(2, 2)),
                "{G}, {T}: Target creature gets +2/+2 until end of turn."));
    }
}
