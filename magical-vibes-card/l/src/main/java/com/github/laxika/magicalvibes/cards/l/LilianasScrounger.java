package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "330")
public class LilianasScrounger extends Card {

    public LilianasScrounger() {
        // At the beginning of each end step, if a creature died this turn, you may put a loyalty
        // counter on a Liliana planeswalker you control.
        var lilianaPlaneswalker = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsPlaneswalkerPredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.LILIANA)
        ));
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(new Morbid(),
                new MayEffect(
                        new PutCounterOnTargetPermanentEffect(CounterType.LOYALTY, 1, lilianaPlaneswalker),
                        "Put a loyalty counter on a Liliana planeswalker you control?")));
    }
}
