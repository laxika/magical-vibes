package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureSharingSacrificedCreatureTypeEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "241")
public class PyreOfHeroes extends Card {

    public PyreOfHeroes() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        SacrificeCreatureCost.withPermanentSnapshot(),
                        new SearchLibraryForCreatureSharingSacrificedCreatureTypeEffect()),
                "{2}, {T}, Sacrifice a creature: Search your library for a creature card that shares a creature "
                        + "type with the sacrificed creature and has mana value equal to 1 plus that creature's "
                        + "mana value. Put that card onto the battlefield, then shuffle. Activate only as a sorcery.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
