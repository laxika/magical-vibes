package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "EVE", collectorNumber = "76")
public class SwirlingSpriggan extends Card {

    public SwirlingSpriggan() {
        // {G/U}{G/U}: Target creature you control becomes the color or colors of your choice
        // until end of turn. The controller picks the colors as the ability resolves.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G/U}{G/U}",
                List.of(new BecomeChosenColorsUntilEndOfTurnEffect()),
                "{G/U}{G/U}: Target creature you control becomes the color or colors of your choice until end of turn.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature you control"
                )
        ));
    }
}
