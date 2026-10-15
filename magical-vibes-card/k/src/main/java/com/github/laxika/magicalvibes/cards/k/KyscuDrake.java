package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsSequenceCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "111")
public class KyscuDrake extends Card {

    public KyscuDrake() {
        // {G}: This creature gets +0/+1 until end of turn. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(false, "{G}", List.of(new BoostSelfEffect(0, 1)),
                "{G}: This creature gets +0/+1 until end of turn. Activate only once each turn.", 1));

        // Sacrifice this creature and a creature named Spitting Drake: Search your library for a card
        // named Viashivan Dragon, put that card onto the battlefield, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentsSequenceCost(
                                List.of(
                                        new PermanentNamedPredicate("Spitting Drake"),
                                        new PermanentIsSourceCardPredicate()
                                ),
                                List.of("a creature named Spitting Drake", "this creature")
                        ),
                        new SearchLibraryEffect(
                                new CardNamedPredicate("Viashivan Dragon"),
                                LibrarySearchDestination.BATTLEFIELD
                        )
                ),
                "Sacrifice this creature and a creature named Spitting Drake: "
                        + "Search your library for a card named Viashivan Dragon, "
                        + "put that card onto the battlefield, then shuffle."
        ));
    }
}
