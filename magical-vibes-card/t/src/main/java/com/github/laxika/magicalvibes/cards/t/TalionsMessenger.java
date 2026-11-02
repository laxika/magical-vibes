package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "73")
@CardRegistration(set = "WOE", collectorNumber = "335")
public class TalionsMessenger extends Card {

    public TalionsMessenger() {
        addTrigger(Triggers.youAttack(), new ConditionalEffect(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.FAERIE)),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardCardThenEffect(
                                null,
                                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentHasSubtypePredicate(CardSubtype.FAERIE),
                                                new PermanentControlledBySourceControllerPredicate()
                                        ))),
                                "a card"))));
    }
}
