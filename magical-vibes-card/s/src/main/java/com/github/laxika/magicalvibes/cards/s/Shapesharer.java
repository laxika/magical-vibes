package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCopyOfTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "85")
public class Shapesharer extends Card {

    public Shapesharer() {
        // Changeling is auto-loaded from Scryfall.
        // {2}{U}: Target Shapeshifter becomes a copy of target creature until your next turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new MakeTargetCopyOfTargetCreatureUntilNextTurnEffect()),
                "{2}{U}: Target Shapeshifter becomes a copy of target creature until your next turn.",
                List.of(
                        new PermanentPredicateTargetFilter(
                                new PermanentHasSubtypePredicate(CardSubtype.SHAPESHIFTER),
                                "Target must be a Shapeshifter."),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature.")
                ),
                2,
                2
        ));
    }
}
