package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "99")
public class BlackPoplarShaman extends Card {

    public BlackPoplarShaman() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new RegenerateEffect(true)),
                "{2}{B}: Regenerate target Treefolk.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.TREEFOLK),
                        "Target must be a Treefolk"
                )
        ));
    }
}
