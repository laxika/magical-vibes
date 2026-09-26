package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "110")
public class RuthlessWaterbender extends Card {

    public RuthlessWaterbender() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new WaterbendCost(2), new BoostSelfEffect(1, 1)),
                "Waterbend {2}: This creature gets +1/+1 until end of turn. Activate only during your turn.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
