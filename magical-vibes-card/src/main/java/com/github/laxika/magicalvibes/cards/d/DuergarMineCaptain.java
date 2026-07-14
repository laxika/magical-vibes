package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "138")
public class DuergarMineCaptain extends Card {

    public DuergarMineCaptain() {
        // {1}{R/W}, {Q}: Attacking creatures get +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{R/W}",
                List.of(new BoostAllCreaturesEffect(1, 0, new PermanentIsAttackingPredicate())),
                "{1}{R/W}, {Q}: Attacking creatures get +1/+0 until end of turn."
        ).withRequiresUntap());
    }
}
