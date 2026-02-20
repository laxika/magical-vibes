package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesToOwnersHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.Set;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "80")
public class DenizenOfTheDeep extends Card {

    public DenizenOfTheDeep() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnCreaturesToOwnersHandEffect(Set.of(
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentTruePredicate(),
                        "Target must be a permanent you control"
                ),
                new PermanentPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                        "Target must not be this permanent"
                )
        )));
    }
}
