package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "222")
@CardRegistration(set = "FIN", collectorNumber = "487")
public class GarnetPrincessOfAlexandria extends Card {

    public GarnetPrincessOfAlexandria() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        ChoosePermanentsEffect.anyNumber(Subjects.controlledBy(Subjects.you(),
                                        new PermanentHasSubtypePredicate(CardSubtype.SAGA)),
                                "any number of Sagas you control"),
                        new RemoveCounterFromSourceEffect(Subjects.thisWay(StepKind.CHOOSE), CounterType.LORE),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new AmountThisWay(StepKind.REMOVE_COUNTERS))),
                "Remove lore counters from any number of Sagas you control?"));
    }
}
