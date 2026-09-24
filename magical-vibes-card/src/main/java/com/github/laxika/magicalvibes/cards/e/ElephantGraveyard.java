package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ARN", collectorNumber = "74")
@CardRegistration(set = "ME4", collectorNumber = "244")
public class ElephantGraveyard extends Card {

    public ElephantGraveyard() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {T}: Regenerate target Elephant.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RegenerateEffect(true)),
                "{T}: Regenerate target Elephant.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.ELEPHANT),
                        "Target must be an Elephant"
                )
        ));
    }
}
