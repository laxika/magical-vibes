package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "20")
public class CliffsideLookout extends Card {

    public CliffsideLookout() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{4}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
