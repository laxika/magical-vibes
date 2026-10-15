package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAuraAttachedToCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ULG", collectorNumber = "6")
public class DevoutHarpist extends Card {

    public DevoutHarpist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DestroyTargetPermanentEffect()),
                "{T}: Destroy target Aura attached to a creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsAuraAttachedToCreaturePredicate(),
                        "Target must be an Aura attached to a creature"
                )
        ));
    }
}
