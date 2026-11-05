package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetWhileHasCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "8")
@CardRegistration(set = "LCC", collectorNumber = "34")
public class XolatoyacTheSmilingFlood extends Card {

    public XolatoyacTheSmilingFlood() {
        var putFloodCounter = new PutCounterOnTargetPermanentEffect(CounterType.FLOOD);
        var grantIsland = new GrantSubtypeToTargetWhileHasCounterEffect(
                CardSubtype.ISLAND, CounterType.FLOOD);

        target(TargetFilters.land())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), putFloodCounter, grantIsland)
                .addTrigger(Triggers.attacks(), putFloodCounter, grantIsland);

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new UntapPermanentsEffect(
                        TapUntapScope.CONTROLLED,
                        new PermanentHasCountersPredicate(CounterType.ANY)));
    }
}
