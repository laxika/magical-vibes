package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedOpponentHasMoreLifeThanAnotherOpponent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "298")
public class BreenaTheDemagogue extends Card {

    public BreenaTheDemagogue() {
        // Whenever a player attacks one of your opponents, if that opponent has more life than
        // another of your opponents, that attacking player draws a card and you put two +1/+1
        // counters on a creature you control.
        addEffect(EffectSlot.ON_ANY_PLAYER_ATTACKS,
                new ConditionalEffect(new AttackedOpponentHasMoreLifeThanAnotherOpponent(),
                        SequenceEffect.of(
                                new DrawCardForTriggeringPlayerEffect(),
                                new PutCounterOnChosenPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE,
                                        2,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentControlledBySourceControllerPredicate()))))));
    }
}
