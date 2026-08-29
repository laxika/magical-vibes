package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "37")
public class WarrenElder extends Card {

    public WarrenElder() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{3}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
