package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "251")
public class SyggRiverGuide extends Card {

    public SyggRiverGuide() {
        // {1}{W}: Target Merfolk you control gains protection from the color of your choice until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect()),
                "{1}{W}: Target Merfolk you control gains protection from the color of your choice until end of turn.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK),
                        "Target must be a Merfolk you control"
                )
        ));
    }
}
