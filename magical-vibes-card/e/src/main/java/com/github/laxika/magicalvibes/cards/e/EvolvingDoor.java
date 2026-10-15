package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithOneMoreColorAndMayCastEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "144")
public class EvolvingDoor extends Card {

    public EvolvingDoor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        SacrificeCreatureCost.withPermanentSnapshot(),
                        new SearchLibraryForCreatureWithOneMoreColorAndMayCastEffect()),
                "{1}, {T}, Sacrifice a creature: Count the colors of the sacrificed creature, then search your "
                        + "library for a creature card that's exactly that many colors plus one. Exile that card, "
                        + "then shuffle. You may cast the exiled card. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
