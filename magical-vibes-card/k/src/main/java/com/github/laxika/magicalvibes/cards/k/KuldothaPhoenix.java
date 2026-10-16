package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.Metalcraft;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "95")
public class KuldothaPhoenix extends Card {

    public KuldothaPhoenix() {
        // Flying, haste (keywords from Scryfall)

        // Metalcraft — {4}: Return this card from your graveyard to the battlefield. Activate only during
        // your upkeep and only if you control three or more artifacts.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build()),
                "Metalcraft — {4}: Return this card from your graveyard to the battlefield. Activate only "
                        + "during your upkeep and only if you control three or more artifacts.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withActivationCondition(new Metalcraft(), "Activate only if you control three or more artifacts"));
    }
}
