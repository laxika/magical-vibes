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

@CardRegistration(set = "LRW", collectorNumber = "103")
public class BoggartLoggers extends Card {

    public BoggartLoggers() {
        // Forestwalk auto-loaded from Scryfall.
        // {2}{B}, Sacrifice this creature: Destroy target Treefolk or Forest.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{2}{B}, Sacrifice Boggart Loggers: Destroy target Treefolk or Forest.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.TREEFOLK, CardSubtype.FOREST)),
                        "Target must be a Treefolk or Forest"
                )
        ));
    }
}
