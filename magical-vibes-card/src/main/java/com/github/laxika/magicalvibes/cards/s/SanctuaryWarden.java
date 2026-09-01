package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "30")
public class SanctuaryWarden extends Card {

    private static final PermanentPredicate COUNTERED_CREATURE_OR_PLANESWALKER = new PermanentAllOfPredicate(List.of(
            new PermanentHasCountersPredicate(CounterType.ANY),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentIsCreaturePredicate(),
                    new PermanentIsPlaneswalkerPredicate()))));

    public SanctuaryWarden() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(2)));

        CreateTokenEffect citizenToken = new CreateTokenEffect(
                "Citizen", 1, 1, CardColor.GREEN,
                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.CITIZEN));
        CardEffect drawAndCreateCitizen = SequenceEffect.of(
                new DrawCardEffect(1), citizenToken);
        CardEffect removeCounterAndReward = new MayEffect(
                SequenceEffect.of(
                        new RemoveCounterFromChosenOwnPermanentEffect(COUNTERED_CREATURE_OR_PLANESWALKER),
                        ConditionalEffect.unless(new EventValueAtLeast(1), drawAndCreateCitizen)),
                "Remove a counter from a creature or planeswalker you control?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, removeCounterAndReward);
        addEffect(EffectSlot.ON_ATTACK, removeCounterAndReward);
    }
}
