package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "7ED", collectorNumber = "28")
@CardRegistration(set = "4ED", collectorNumber = "37")
public class NorthernPaladin extends Card {

    public NorthernPaladin() {
        // {W}{W}, {T}: Destroy target black permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new DestroyTargetPermanentEffect(false)),
                "{W}{W}, {T}: Destroy target black permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                        "Target must be a black permanent"
                )
        ));
    }
}
