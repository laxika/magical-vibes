package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "226")
@CardRegistration(set = "MH2", collectorNumber = "128")
public class Gargadon extends Card {

    public Gargadon() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(),
                "Suspend 4\u2014{1}{R}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
