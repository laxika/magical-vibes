package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "186")
public class GoblinDiggingTeam extends Card {

    public GoblinDiggingTeam() {
        // {T}, Sacrifice this creature: Destroy target Wall.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{T}, Sacrifice this creature: Destroy target Wall.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Target must be a Wall"
                )
        ));
    }
}
