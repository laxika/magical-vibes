package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "78")
public class SeafloorStalker extends Card {

    public SeafloorStalker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}",
                List.of(
                        new ReduceActivationCostEffect(new PartySize()),
                        new BoostSelfEffect(1, 0),
                        new MakeCreatureUnblockableEffect(true)
                ),
                "{4}{U}: This creature gets +1/+0 until end of turn and can't be blocked this turn. "
                        + "This ability costs {1} less to activate for each creature in your party."
        ));
    }
}
