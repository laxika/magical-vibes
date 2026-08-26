package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "167")
public class KeldonHalberdier extends Card {

    public KeldonHalberdier() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(),
                "Suspend 4\u2014{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
