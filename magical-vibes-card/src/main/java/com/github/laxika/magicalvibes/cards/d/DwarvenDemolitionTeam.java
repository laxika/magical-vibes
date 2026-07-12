package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "184")
public class DwarvenDemolitionTeam extends Card {

    public DwarvenDemolitionTeam() {
        // {T}: Destroy target Wall.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DestroyTargetPermanentEffect()),
                "{T}: Destroy target Wall.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.WALL),
                        "Target must be a Wall"
                )
        ));
    }
}
