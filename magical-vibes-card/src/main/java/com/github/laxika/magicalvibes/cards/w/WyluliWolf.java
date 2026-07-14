package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "270")
public class WyluliWolf extends Card {

    public WyluliWolf() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(new BoostTargetCreatureEffect(1, 1)),
                "{T}: Target creature gets +1/+1 until end of turn."));
    }
}
