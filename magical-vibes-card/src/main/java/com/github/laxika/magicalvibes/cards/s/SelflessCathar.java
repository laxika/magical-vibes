package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "30")
public class SelflessCathar extends Card {

    public SelflessCathar() {
        // {1}{W}, Sacrifice Selfless Cathar: Creatures you control get +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new SacrificeSelfCost(), new BoostAllOwnCreaturesEffect(1, 1)),
                "{1}{W}, Sacrifice Selfless Cathar: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
