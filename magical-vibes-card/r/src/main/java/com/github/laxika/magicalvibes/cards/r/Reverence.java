package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "SOK", collectorNumber = "26")
public class Reverence extends Card {

    public Reverence() {
        addStaticEffect(new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentPowerAtLeastPredicate(3)));
    }
}
