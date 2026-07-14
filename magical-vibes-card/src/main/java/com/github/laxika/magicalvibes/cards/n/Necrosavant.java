package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "145")
public class Necrosavant extends Card {

    public Necrosavant() {
        // {3}{B}{B}, Sacrifice a creature: Return this card from your graveyard to the
        // battlefield. Activate only during your upkeep.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}{B}",
                List.of(
                        new SacrificeCreatureCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()),
                "{3}{B}{B}, Sacrifice a creature: Return this card from your graveyard to the "
                        + "battlefield. Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ));
    }
}
