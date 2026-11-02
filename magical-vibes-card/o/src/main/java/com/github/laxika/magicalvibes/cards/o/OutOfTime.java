package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PhaseOutAllCreaturesUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "23")
@CardRegistration(set = "WHO", collectorNumber = "209")
@CardRegistration(set = "WHO", collectorNumber = "467")
@CardRegistration(set = "WHO", collectorNumber = "800")
@CardRegistration(set = "WHO", collectorNumber = "1058")
public class OutOfTime extends Card {

    public OutOfTime() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PhaseOutAllCreaturesUntilSourceLeavesEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME));
    }
}
