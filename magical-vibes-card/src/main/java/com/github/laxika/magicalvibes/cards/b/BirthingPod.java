package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithExactMVToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "NPH", collectorNumber = "104")
public class BirthingPod extends Card {

    public BirthingPod() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{G/P}",
                List.of(
                        new SacrificeCreatureCost(true),
                        new SearchLibraryForCreatureWithExactMVToBattlefieldEffect(1)
                ),
                "{1}{G/P}, {T}, Sacrifice a creature: Search your library for a creature card with mana value equal to 1 plus the sacrificed creature's mana value, put that card onto the battlefield, then shuffle.",
                null,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
