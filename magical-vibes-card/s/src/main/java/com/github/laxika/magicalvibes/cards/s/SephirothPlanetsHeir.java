package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "505")
@CardRegistration(set = "FIN", collectorNumber = "553")
public class SephirothPlanetsHeir extends Card {

    public SephirothPlanetsHeir() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new BoostAllCreaturesEffect(-2, -2,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
