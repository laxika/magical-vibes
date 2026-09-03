package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "37")
public class InfiltratorIlKor extends Card {

    public InfiltratorIlKor() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(),
                "Suspend 2—{1}{U}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(2));
    }
}
