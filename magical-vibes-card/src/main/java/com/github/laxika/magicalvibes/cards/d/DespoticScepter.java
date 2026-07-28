package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "316")
public class DespoticScepter extends Card {

    public DespoticScepter() {
        // {T}: Destroy target permanent you own. It can't be regenerated.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect(true)),
                "{T}: Destroy target permanent you own. It can't be regenerated.",
                new OwnedPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you own"
                )
        ));
    }
}
