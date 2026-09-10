package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "113")
public class OverlordOfTheBalemurk extends Card {

    public OverlordOfTheBalemurk() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}"))));

        SequenceEffect millThenReturn = SequenceEffect.of(
                new MillEffect(4, MillRecipient.CONTROLLER),
                new MayEffect(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardAllOfPredicate(List.of(
                                                new CardTypePredicate(CardType.CREATURE),
                                                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.AVATAR)))),
                                        new CardTypePredicate(CardType.PLANESWALKER))))
                                .build(),
                        "Return a non-Avatar creature card or a planeswalker card from your graveyard to your hand?"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, millThenReturn);
        addEffect(EffectSlot.ON_ATTACK, millThenReturn);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForAlternateCost(),
                new EnterWithCountersEffect(CounterType.TIME, new Fixed(5))));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new CastForAlternateCost(),
                        new SourceCounterThreshold(1, CounterType.TIME))),
                new RemoveCounterFromSourceEffect(CounterType.TIME, 1)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AllOf(List.of(
                        new CastForAlternateCost(),
                        new SourceCounterThreshold(1, CounterType.TIME))),
                new SetCardTypesEffect(Set.of(CardType.ENCHANTMENT), GrantScope.SELF)));
    }
}
