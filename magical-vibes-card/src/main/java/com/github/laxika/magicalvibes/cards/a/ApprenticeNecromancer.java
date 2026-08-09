package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "51")
public class ApprenticeNecromancer extends Card {

    public ApprenticeNecromancer() {
        // {B}, {T}, Sacrifice this creature: Return target creature card from your graveyard to the
        // battlefield. That creature gains haste. At the beginning of the next end step, sacrifice it.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .targetGraveyard(true)
                                .grantHaste(true)
                                .sacrificeAtEndStep(true)
                                .build()
                ),
                "{B}, {T}, Sacrifice this creature: Return target creature card from your graveyard to the battlefield. "
                        + "That creature gains haste. At the beginning of the next end step, sacrifice it."
        ));
    }
}
