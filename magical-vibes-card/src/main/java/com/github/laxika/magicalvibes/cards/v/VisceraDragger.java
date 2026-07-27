package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "92")
public class VisceraDragger extends Card {

    public VisceraDragger() {
        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");

        // Unearth {1}{B}: Return this card from your graveyard to the battlefield. It gains haste.
        // Exile it at the beginning of the next end step. Unearth only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .grantHaste(true)
                        .exileAtEndStep(true)
                        .build()),
                "Unearth {1}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
