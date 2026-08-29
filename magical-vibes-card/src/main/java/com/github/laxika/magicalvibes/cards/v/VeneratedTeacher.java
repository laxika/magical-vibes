package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasNonManaActivatedAbilityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "93")
public class VeneratedTeacher extends Card {

    public VeneratedTeacher() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnEachControlledPermanentEffect(
                CounterType.LEVEL,
                2,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        PermanentHasNonManaActivatedAbilityPredicate.levelUp()
                ))
        ));
    }
}
