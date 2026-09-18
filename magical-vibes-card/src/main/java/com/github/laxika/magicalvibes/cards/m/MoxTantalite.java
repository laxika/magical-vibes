package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "226")
@CardRegistration(set = "SLZ", collectorNumber = "107")
@CardRegistration(set = "SLZ", collectorNumber = "228")
@CardRegistration(set = "SLZ", collectorNumber = "349")
public class MoxTantalite extends Card {

    public MoxTantalite() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(),
                "Suspend 3—{0}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
