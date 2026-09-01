package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "12")
public class DualSunAdepts extends Card {

    public DualSunAdepts() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{5}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
