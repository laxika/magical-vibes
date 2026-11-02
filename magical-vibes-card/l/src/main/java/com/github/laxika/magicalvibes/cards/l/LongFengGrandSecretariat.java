package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentControllerConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "233")
public class LongFengGrandSecretariat extends Card {

    public LongFengGrandSecretariat() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.dies(null, ObjectRelation.ANY),
                        // "Another creature you control or a land you control": Long Feng's own death is
                        // covered by its dies trigger below, and only when it was a land.
                        new TriggeringCardConditionalEffect(
                                new CardAllOfPredicate(List.of(
                                        new CardNotPredicate(new CardIsSelfPredicate()),
                                        new CardAnyOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardTypePredicate(CardType.LAND)
                                        ))
                                )),
                                new TriggeringPermanentControllerConditionalEffect(
                                        new PutCounterOnTargetPermanentEffect(
                                                CounterType.PLUS_ONE_PLUS_ONE, 1))))
                .addTrigger(Triggers.dies(Triggers.LAND, ObjectRelation.THIS),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
