package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "85")
public class BoggartArsonists extends Card {

    public BoggartArsonists() {
        // Plainswalk auto-loaded from Scryfall.
        // {2}{R}, Sacrifice Boggart Arsonists: Destroy target Scarecrow or Plains.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{2}{R}, Sacrifice Boggart Arsonists: Destroy target Scarecrow or Plains.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.SCARECROW, CardSubtype.PLAINS)),
                        "Target must be a Scarecrow or Plains"
                )
        ));
    }
}
