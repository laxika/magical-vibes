package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "36")
public class SeaGateBanneret extends Card {

    public SeaGateBanneret() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{4}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
