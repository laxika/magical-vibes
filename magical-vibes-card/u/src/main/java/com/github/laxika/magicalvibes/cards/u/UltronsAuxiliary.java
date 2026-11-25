package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "669")
public class UltronsAuxiliary extends Card {

    public UltronsAuxiliary() {
        PutCountersEffect counter = PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE);
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        ObjectRelation.YOU_OWN),
                counter);
        addTrigger(Triggers.putIntoGraveyardFromAnywhereButBattlefield(Triggers.NONTOKEN_ARTIFACT, ObjectRelation.YOU_OWN),
                counter);
    }
}
