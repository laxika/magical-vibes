package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "71")
public class RewriteHistory extends Card {

    public RewriteHistory() {
        CardEffect loot = SequenceEffect.of(
                new DrawCardEffect(),
                new DiscardEffect(1, Subjects.you()),
                PutCountersEffect.onSelf(CounterType.PLAN));
        addTrigger(Triggers.permanentYouControlBecomesTapped()
                        .withFilter(new PermanentIsCreaturePredicate()),
                TriggerOptions.ONE_OR_MORE,
                loot);

        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        // "When the fourth plan counter is put" is an event trigger: removing counters in response does not stop it.
        addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT.withEventCondition(
                new SourceCounterThreshold(4, CounterType.PLAN)),
                SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(instantOrSorcery, 2)))));
    }
}
