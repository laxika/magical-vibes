package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "14")
@CardRegistration(set = "DSC", collectorNumber = "45")
public class TheyCameFromThePipes extends Card {

    public TheyCameFromThePipes() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                ManifestDreadEffect.forController(),
                ManifestDreadEffect.forController()));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsFaceDownPredicate())),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardEffect(1));
    }
}
