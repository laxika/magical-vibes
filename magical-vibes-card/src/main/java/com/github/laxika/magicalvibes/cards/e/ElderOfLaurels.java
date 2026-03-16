package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreaturePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "177")
public class ElderOfLaurels extends Card {

    public ElderOfLaurels() {
        // {3}{G}: Target creature gets +X/+X until end of turn, where X is the number of creatures you control.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostTargetCreaturePerControlledPermanentEffect(1, 1, new PermanentIsCreaturePredicate())),
                "{3}{G}: Target creature gets +X/+X until end of turn, where X is the number of creatures you control."
        ));
    }
}
