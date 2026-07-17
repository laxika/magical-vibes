package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "392")
public class ObeliskOfUndoing extends Card {

    public ObeliskOfUndoing() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(ReturnToHandEffect.target()),
                "{6}, {T}: Return target permanent you both own and control to your hand.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentOwnedBySourceControllerPredicate(),
                        "Target must be a permanent you both own and control"
                )
        ));
    }
}
