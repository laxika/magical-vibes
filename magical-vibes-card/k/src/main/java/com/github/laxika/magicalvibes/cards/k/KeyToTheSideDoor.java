package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesNameWithLegendaryControlledPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "175")
public class KeyToTheSideDoor extends Card {

    public KeyToTheSideDoor() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new MakeCreatureUnblockableEffect()),
                "{2}, {T}: Target creature can't be blocked this turn.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(
                                new CardSharesNameWithLegendaryControlledPermanentPredicate(),
                                "legendary card with the same name as a legendary permanent you control"),
                        new DrawCardEffect(2)
                ),
                "{1}, {T}, Discard a legendary card with the same name as a legendary permanent you control: Draw two cards."
        ));
    }
}
