package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "131")
public class CursedRecording extends Card {

    public CursedRecording() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.TIME),
                        new ConditionalEffect(
                                new SourceCounterThreshold(7, CounterType.TIME),
                                SequenceEffect.of(
                                        RemoveCountersEffect.allFromSelf(CounterType.TIME),
                                        DealDamageEffect.to(Subjects.you(), 20)))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                                CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect()))),
                "{T}: When you next cast an instant or sorcery spell this turn, copy that spell. "
                        + "You may choose new targets for the copy."));
    }
}
