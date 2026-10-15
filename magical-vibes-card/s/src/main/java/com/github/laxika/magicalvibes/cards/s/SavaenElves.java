package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "84")
public class SavaenElves extends Card {

    public SavaenElves() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{G}",
                List.of(new DestroyTargetPermanentEffect()),
                "{G}{G}, {T}: Destroy target Aura attached to a land.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAuraAttachedToLandPredicate(),
                        "Target must be an Aura attached to a land"
                )
        ));
    }
}
