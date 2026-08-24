package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "60")
public class ErrantEphemeron extends Card {

    public ErrantEphemeron() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 4—{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
