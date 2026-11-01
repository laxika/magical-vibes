package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "173")
public class TectonicInstability extends Card {

    public TectonicInstability() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new TapPermanentsEffect(TapUntapScope.ALL_PERMANENTS, new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentControlledBySourceControllerPredicate()))));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS),
                new TapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS,
                        new PermanentIsLandPredicate()));
    }
}
