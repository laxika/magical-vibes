package com.github.laxika.magicalvibes.cards.t;

import java.util.List;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

@CardRegistration(set = "SHM", collectorNumber = "130")
public class ToilToRenown extends Card {

    public ToilToRenown() {
        // Each tapped permanent is counted once, so an artifact creature only adds 1 life.
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTappedPredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsLandPredicate()
                        ))
                )),
                CountScope.CONTROLLER
        )));
    }
}
