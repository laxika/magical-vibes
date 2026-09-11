package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "131")
public class CursedRecording extends Card {

    public CursedRecording() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                instantOrSorcery,
                List.of(SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.TIME),
                        new ConditionalEffect(
                                new SourceCounterThreshold(7, CounterType.TIME),
                                SequenceEffect.of(
                                        new RemoveAllCountersEffect(CounterType.TIME),
                                        new DealDamageToPlayersEffect(20, DamageRecipient.CONTROLLER)))))));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new CopyNextInstantOrSorceryCastThisTurnEffect()),
                "{T}: When you next cast an instant or sorcery spell this turn, copy that spell. "
                        + "You may choose new targets for the copy."));
    }
}
