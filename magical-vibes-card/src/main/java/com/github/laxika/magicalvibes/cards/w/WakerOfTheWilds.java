package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutXPlusOnePlusOneCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "215")
public class WakerOfTheWilds extends Card {

    public WakerOfTheWilds() {
        // {X}{G}{G}: Put X +1/+1 counters on target land you control.
        // That land becomes a 0/0 Elemental creature with haste. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{X}{G}{G}",
                List.of(
                        new PutXPlusOnePlusOneCountersOnTargetPermanentEffect(),
                        new AnimateTargetPermanentEffect(
                                0, 0,
                                List.of(CardSubtype.ELEMENTAL),
                                Set.of(Keyword.HASTE)
                        )
                ),
                "{X}{G}{G}: Put X +1/+1 counters on target land you control. That land becomes a 0/0 Elemental creature with haste. It's still a land.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land you control"
                )
        ));
    }
}
