package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPCA", collectorNumber = "78")
public class Tazeem extends Card {

    public Tazeem() {
        addStaticEffect(new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                new PermanentIsCreaturePredicate(),
                new PermanentIsCreaturePredicate(),
                "Creatures can't block"));
        addTrigger(Triggers.chaosEnsues(), new DrawCardEffect(
                new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER)));
    }
}
