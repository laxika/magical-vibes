package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "129")
public class GiantDustwasp extends Card {

    public GiantDustwasp() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(),
                "Suspend 4—{1}{G}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(4));
    }
}
