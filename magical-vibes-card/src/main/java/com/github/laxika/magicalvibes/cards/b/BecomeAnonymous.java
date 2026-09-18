package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNontokenCreatureAndTopCardsThenCloakEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "14")
public class BecomeAnonymous extends Card {

    public BecomeAnonymous() {
        PermanentAllOfPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        target(new OwnedPermanentPredicateTargetFilter(
                targetFilter, "Target must be a nontoken creature you own"))
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetNontokenCreatureAndTopCardsThenCloakEffect());
    }
}
