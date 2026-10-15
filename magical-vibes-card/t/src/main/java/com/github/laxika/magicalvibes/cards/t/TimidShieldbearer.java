package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "39")
public class TimidShieldbearer extends Card {

    public TimidShieldbearer() {
        // {4}{W}: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{4}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
