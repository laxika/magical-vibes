package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockCreaturesWithPowerAtLeastEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "259")
public class Orgg extends Card {

    public Orgg() {
        // Can't attack if defending player controls an untapped creature with power 3 or greater
        // == can't attack unless the defending player controls NO such creature.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new NotCondition(new DefendingPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(3),
                        new PermanentNotPredicate(new PermanentIsTappedPredicate())
                )))),
                "defending player controls no untapped creature with power 3 or greater"
        ));

        // Can't block creatures with power 3 or greater.
        addEffect(EffectSlot.STATIC, new CantBlockCreaturesWithPowerAtLeastEffect(3));
    }
}
