package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "9")
public class DwarvenProvisioner extends Card {

    public DwarvenProvisioner() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{3}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
