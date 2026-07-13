package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "7ED", collectorNumber = "46")
public class SouthernPaladin extends Card {

    public SouthernPaladin() {
        // {W}{W}, {T}: Destroy target red permanent.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}{W}",
                List.of(new DestroyTargetPermanentEffect(false)),
                "{W}{W}, {T}: Destroy target red permanent.",
                new PermanentPredicateTargetFilter(
                        new PermanentColorInPredicate(Set.of(CardColor.RED)),
                        "Target must be a red permanent"
                )
        ));
    }
}
