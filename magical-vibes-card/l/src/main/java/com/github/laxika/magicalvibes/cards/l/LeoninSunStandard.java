package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "194")
public class LeoninSunStandard extends Card {

    public LeoninSunStandard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new BoostAllOwnCreaturesEffect(1, 1)),
                "{1}{W}: Creatures you control get +1/+1 until end of turn."
        ));
    }
}
