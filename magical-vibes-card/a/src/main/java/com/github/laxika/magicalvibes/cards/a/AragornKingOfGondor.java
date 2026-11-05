package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "5")
@CardRegistration(set = "LTC", collectorNumber = "85")
@CardRegistration(set = "LTC", collectorNumber = "448")
public class AragornKingOfGondor extends Card {

    public AragornKingOfGondor() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());

        var cantBlockThisTurn = new CantBlockThisTurnEffect(TapUntapScope.TARGET);
        target(TargetFilters.creature(), 0, 1)
                .bindEffect(cantBlockThisTurn);
        addTrigger(Triggers.attacks(),
                cantBlockThisTurn,
                new ConditionalEffect(
                new ControllerIsMonarch(),
                new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES)));
    }
}
