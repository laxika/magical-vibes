package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "214")
public class SteelOverseer extends Card {

    public SteelOverseer() {
        // {T}: Put a +1/+1 counter on each artifact creature you control.
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        ))
                )),
                "{T}: Put a +1/+1 counter on each artifact creature you control."));
    }
}
