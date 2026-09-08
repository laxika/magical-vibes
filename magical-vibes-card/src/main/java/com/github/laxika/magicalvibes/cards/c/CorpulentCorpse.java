package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "98")
public class CorpulentCorpse extends Card {

    public CorpulentCorpse() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(),
                "Suspend 5\u2014{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(5));
    }
}
